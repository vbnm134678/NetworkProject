package Client;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

public class Weather implements Runnable {

	private ChatterUI ui;
	
	public Weather(ChatterUI ui) {
		this.ui = ui;
	}
	
	@Override
	public void run() {
		JsonArray parsed = null;
		try {
			parsed = Weather.parseData();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for (int i = parsed.size() - 1; i >= 0; i--) {
			JsonObject weather_i = parsed.get(i).getAsJsonObject();
			ui.addNewPublicDataPanel(weather_i);
		}
		
		
	}
	
	public static JsonArray parseData() throws IOException {

		Date now = new Date();
		SimpleDateFormat date = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat time_ = new SimpleDateFormat("HH");

		String nx = "60"; // 위도
		String ny = "127"; // 경도
		String pageNo = "1";
		String numOfRows = "40";
		String base_date = date.format(now);
		String base_time = time_.format(now);

		base_time = base_time + "00";

		switch (base_time) {

		case "0200":
		case "0300":
		case "0400":
			base_time = "0200";
			break;
		case "0500":
		case "0600":
		case "0700":
			base_time = "0500";
			break;
		case "0800":
		case "0900":
		case "1000":
			base_time = "0800";
			break;
		case "1100":
		case "1200":
		case "1300":
			base_time = "1100";
			break;
		case "1400":
		case "1500":
		case "1600":
			base_time = "1400";
			break;
		case "1700":
		case "1800":
		case "1900":
			base_time = "1700";
			break;
		case "2000":
		case "2100":
		case "2200":
			base_time = "2000";
			break;
		default:
			now = new Date(now.getTime() + (1000 * 60 * 60 * 24 * -1));
			base_time = "2300";

		}

		String apiUrl = "http://apis.data.go.kr/1360000/VilageFcstInfoService/getVilageFcst";
		String serviceKey = "8QmT6x7OcMmCwBXKFDWiFGWCMXITSPC1X5s57OxBYzSr6U8uAr4GqVFotkzSlpVvmdnkcLp9TmXRoWoLif8Dfg%3D%3D";

		StringBuilder urlBuilder = new StringBuilder(apiUrl);
		urlBuilder.append("?" + URLEncoder.encode("serviceKey", "UTF-8") + "=" + serviceKey);
		urlBuilder.append("&" + URLEncoder.encode("nx", "UTF-8") + "=" + URLEncoder.encode(nx, "UTF-8")); // 경도
		urlBuilder.append("&" + URLEncoder.encode("ny", "UTF-8") + "=" + URLEncoder.encode(ny, "UTF-8")); // 위도
		urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=" + URLEncoder.encode(pageNo, "UTF-8"));
		urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode(numOfRows, "UTF-8"));
		urlBuilder.append("&" + URLEncoder.encode("base_date", "UTF-8") + "="
				+ URLEncoder.encode(base_date, "UTF-8")); /* 조회하고싶은 날짜 */
		urlBuilder.append("&" + URLEncoder.encode("base_time", "UTF-8") + "="
				+ URLEncoder.encode(base_time, "UTF-8")); /* 조회하고싶은 시간 AM 02시부터 3시간 단위 */
		urlBuilder.append("&" + URLEncoder.encode("dataType", "UTF-8") + "=" + URLEncoder.encode("json", "UTF-8")); // 데이터타입

		URL url = new URL(urlBuilder.toString());
		// 어떻게 넘어가는지 확인하고 싶으면 아래 출력분 주석 해제
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Content-Type", "application/json");
		BufferedReader rd;
		if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		} else {
			rd = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
		}
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = rd.readLine()) != null) {
			sb.append(line);
		}
		rd.close();
		conn.disconnect();
		String result = sb.toString();

		JsonParser Parser = new JsonParser();
		JsonObject obj = (JsonObject) Parser.parse(result);
		JsonObject response = (JsonObject) obj.get("response");
		JsonObject body = (JsonObject) response.get("body");
		JsonObject items = (JsonObject) body.get("items");
		JsonArray item = (JsonArray) items.get("item");

		String category;
		JsonObject weather;
		String day = "";
		String time = "";
		String value = "";

		JsonArray weatherInfo = new JsonArray();
		JsonObject oneWeather = new JsonObject();

		for (int i = 0; i < item.size(); i++) {

			weather = (JsonObject) item.get(i);

			Object fcstDate = weather.get("fcstDate").getAsString();
			Object fcstTime = weather.get("fcstTime").getAsString();
			category = weather.get("category").getAsString();

			if (!day.equals(fcstDate.toString())) {
				day = fcstDate.toString();
			}
			if (!time.equals(fcstTime.toString())) {
				time = fcstTime.toString();
				//System.out.println("\n" + day + "  " + "예보시각: " + time);
			}
			oneWeather.addProperty("day", day);
			oneWeather.addProperty("time", time);

			if (category.equals("T3H")) {
				category = "기온: ";
				Object fcstValue = weather.get("fcstValue").getAsString();
				fcstValue = fcstValue + "℃";
				//System.out.println(category + fcstValue);
				oneWeather.addProperty("temperature", fcstValue.toString());

				weatherInfo.add(oneWeather); // TODO
				oneWeather = new JsonObject();
			}

			else if (category.equals("POP")) {
				category = "강수확률: ";
				Object fcstValue = weather.get("fcstValue").getAsString();
				fcstValue = fcstValue + "%";
				//System.out.println(category + fcstValue);
				oneWeather.addProperty("precipitation", fcstValue.toString());
			}

			else if (category.equals("REH")) {
				category = "습도: ";
				Object fcstValue = weather.get("fcstValue").getAsString();
				fcstValue = fcstValue + "%";
				//System.out.println(category + fcstValue);
				oneWeather.addProperty("humidity", fcstValue.toString());
			}

			else if (category.equals("SKY")) {
				category = "하늘: ";
				Object fcstValue = weather.get("fcstValue").getAsString();

				if (fcstValue.equals("1")) {
					fcstValue = "맑음";

				} else if (fcstValue.equals("2")) {
					fcstValue = "비";
				} else if (fcstValue.equals("3")) {
					fcstValue = "구름많음";

				} else if (fcstValue.equals("4")) {
					fcstValue = "흐림";
				}
				//System.out.println(category + fcstValue);
				oneWeather.addProperty("sky", fcstValue.toString());
			}

		}
		//System.out.println(weatherInfo);
		
		return weatherInfo;
	}


}
