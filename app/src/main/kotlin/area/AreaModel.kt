package area

import android.app.Application
import android.text.format.DateUtils
import android.util.TypedValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import element.ElementsRepo
import element.bitcoinSurveyDate
import element.name
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonPrimitive
import map.boundingBox
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.btcmap.R
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory

class AreaModel(
    private val areasRepo: AreasRepo,
    private val elementsRepo: ElementsRepo,
    private val app: Application,
) : AndroidViewModel(app) {

    private val _state = MutableStateFlow<State>(State.Loading)
    val state = _state.asStateFlow()

    fun setArgs(args: Args) {
        viewModelScope.launch {
            val area = areasRepo.selectById(args.areaId)!!

            val polygons = area.tags.polygons()
            val boundingBox = boundingBox(polygons)
            val geometryFactory = GeometryFactory()

            val elements = elementsRepo.selectByBoundingBox(
                minLat = boundingBox.latSouth,
                maxLat = boundingBox.latNorth,
                minLon = boundingBox.lonWest,
                maxLon = boundingBox.lonEast,
            )
                .asSequence()
                .filter { element ->
                    polygons.any {
                        val coordinate = Coordinate(element.lon, element.lat)
                        it.contains(geometryFactory.createPoint(coordinate))
                    }
                }
                .sortedByDescending { it.osmTags.bitcoinSurveyDate() }
                .map {
                    val status: String
                    val colorResId: Int

                    val surveyDate = it.osmTags.bitcoinSurveyDate()

                    if (surveyDate != null) {
                        val date = DateUtils.getRelativeDateTimeString(
                            app,
                            surveyDate.toEpochSecond() * 1000,
                            DateUtils.SECOND_IN_MILLIS,
                            DateUtils.WEEK_IN_MILLIS,
                            0,
                        ).split(",").first()

                        status = date
                        colorResId = com.google.android.material.R.attr.colorOnSurface
                    } else {
                        status = app.getString(R.string.not_verified)
                        colorResId = com.google.android.material.R.attr.colorError
                    }

                    AreaAdapter.Item.Element(
                        id = it.id,
                        iconId = it.icon.ifBlank { "question_mark" },
                        name = it.osmTags.name(app.resources),
                        status = status,
                        colorResId = colorResId,
                        showCheckmark = surveyDate != null,
                    )
                }.sortedBy { !it.showCheckmark }.toMutableList()

            val boundingBoxPaddingPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                16f,
                app.resources.displayMetrics,
            ).toInt()

            val map = AreaAdapter.Item.Map(
                polygons = area.tags.polygons(),
                paddingPx = boundingBoxPaddingPx,
            )

            val contact = AreaAdapter.Item.Contact(
                website = area.tags["contact:website"]?.jsonPrimitive?.content?.toHttpUrlOrNull(),
                twitter = area.tags["contact:twitter"]?.jsonPrimitive?.content?.toHttpUrlOrNull(),
                telegram = area.tags["contact:telegram"]?.jsonPrimitive?.content?.toHttpUrlOrNull(),
                discord = area.tags["contact:discord"]?.jsonPrimitive?.content?.toHttpUrlOrNull(),
                youtube = area.tags["contact:youtube"]?.jsonPrimitive?.content?.toHttpUrlOrNull(),
            )

            _state.update {
                State.Loaded(
                    area = area,
                    items = listOf(map, contact) + elements,
                )
            }
        }
    }

    suspend fun selectArea(id: String): Area? {
        return areasRepo.selectById(id)
    }

    data class Args(
        val areaId: String,
    )

    sealed class State {

        object Loading : State()

        data class Loaded(
            val area: Area,
            val items: List<AreaAdapter.Item>,
        ) : State()
    }
}