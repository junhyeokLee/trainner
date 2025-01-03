package com.sports2i.trainer.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class WeatherData(
    val coord: Coord,
    val weather: List<Weather>,
    val base: String,
    val main: Main,
    val visibility: Int,
    val wind: Wind,
    val clouds: Clouds,
    val dt: Long,
    val sys: Sys,
    val timezone: Int,
    val id: Int,
    val name: String,
    val cod: Int
) : Parcelable {
    override fun toString(): String {
        return "WeatherData(coord=$coord, weather=$weather, base='$base', main=$main, " +
                "visibility=$visibility, wind=$wind, clouds=$clouds, dt=$dt, sys=$sys, " +
                "timezone=$timezone, id=$id, name='$name', cod=$cod)"
    }
}

@Parcelize
data class Coord(
    val lon: Double,
    val lat: Double
) : Parcelable

@Parcelize
data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
) : Parcelable

@Parcelize
data class Main(
    val temp: Double,
    val feels_like: Double,
    val temp_min: Double,
    val temp_max: Double,
    val pressure: Int,
    val humidity: Int
) : Parcelable

@Parcelize
data class Wind(
    val speed: Double,
    val deg: Int
) : Parcelable

@Parcelize
data class Clouds(
    val all: Int
) : Parcelable

@Parcelize
data class Sys(
    val type: Int,
    val id: Int,
    val country: String,
    val sunrise: Long,
    val sunset: Long
) : Parcelable


//coord.lon 위치의 경도
//coord.lat 위치의 위도

//weather (추가 정보 기상 조건 코드 )
//weather.id 기상 조건 ID
//weather.main 날씨 매개변수 그룹(비, 눈, 구름 등) Rain, Snow, Clouds Clear 등
//weather.description 그룹 내 날씨 상태
//weather.icon 날씨 아이콘 ID

//base 내부 매개변수

//main
//main.temp 온도. 단위 기본값: 켈빈, 미터법: 섭씨, 영국식: 화씨
//main.feels_like 온도. 이 온도 매개변수는 날씨에 대한 인간의 인식을 설명합니다. 단위 기본값: 켈빈, 미터법: 섭씨, 영국식: 화씨
//main.pressure 해수면의 대기압, hPa
//main.humidity 습도, %
//main.temp_min 현재 최저기온입니다. 이는 현재 관측된 최소 온도입니다(대규모 대도시 및 도시 지역 내). 자세한 내용은 여기에서 확인하세요 . 단위 기본값: 켈빈, 미터법: 섭씨, 영국식: 화씨
//main.temp_max 현재 최고온도. 이는 현재 관찰된 최대 온도입니다(대규모 대도시 및 도시 지역 내). 자세한 내용은 여기에서 확인하세요 . 단위 기본값: 켈빈, 미터법: 섭씨, 영국식: 화씨
//main.sea_level 해수면의 대기압, hPa
//main.grnd_level 지면의 대기압, hPa
//visibility 가시성, 미터. 가시성의 최대 값은 10km입니다

//wind
//wind.speed 바람 속도. 단위 기본값: 미터/초, 미터법: 미터/초, 영국식: 마일/시간
//wind.deg 풍향, 각도(기상)
//wind.gust 돌풍. 단위 기본값: 미터/초, 미터법: 미터/초, 영국식: 마일/시간

//clouds
//clouds.all흐림, %
//rain
//rain.1h (가능한 경우) 지난 1시간 동안의 강우량(mm). 이 매개변수에는 측정 단위로 mm만 사용할 수 있습니다.
//rain.3h (가능한 경우) 지난 3시간 동안의 강우량(mm). 이 매개변수에는 측정 단위로 mm만 사용할 수 있습니다.
//snow
//snow.1h (가능한 경우) 지난 1시간 동안의 적설량, mm. 이 매개변수에는 측정 단위로 mm만 사용할 수 있습니다.
//snow.3h (가능한 경우) 지난 3시간 동안의 적설량(mm). 이 매개변수에는 측정 단위로 mm만 사용할 수 있습니다.

//dt 데이터 계산 시간, unix, UTC

//sys
//sys.type내부 매개변수
//sys.id내부 매개변수
//sys.message내부 매개변수
//sys.country국가 코드(GB, JP 등)
//sys.sunrise일출 시간, 유닉스, UTC
//sys.sunset일몰 시간, 유닉스, UTC

//timezone UTC에서 초 단위로 이동
//id 도시 ID
//name 도시 이름
//cod 내부 매개변수
