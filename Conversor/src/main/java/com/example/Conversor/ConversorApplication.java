package com.example.Conversor;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Scanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@SpringBootApplication
public class ConversorApplication {
	
	public static URI getRelativePath(String path) {
		String url = "https://v6.exchangerate-api.com/v6/903b80dd483e81436df2b9c0/" + path;
		try {
			return new URI(url);
		} catch (URISyntaxException e) {
			System.out.println("Incorrect URI creation");
			return null;
		}
	}
	
	public static Map<String, String> convertJson(String json) {
		Gson gson = new Gson();
		Type string = new TypeToken<Map<String, String>>() {}.getType();
		Map<String, String> map = gson.fromJson(json, string);
		return map;
	}
	
	public static String getConversionRates(String json) {
		int divices = json.indexOf("\"conversion_rates\":") + 19;
		int end = json.length() - 2;
		
		return json.substring(divices, end);
	}
	
	public static void printCurrencies(Map<String, String> currencies) {
		int counter = 0;
		for (String key : currencies.keySet()) {
			System.out.print(key + "\t");
			counter++;
			if (counter >= 15) {
				System.out.println("");
				counter = 0;
			}
		}
		System.out.println("");
	}
	
	public static String requestCurrencies(URI path) {
		HttpClient client = HttpClient.newHttpClient();
		try {
			HttpRequest req = HttpRequest.newBuilder()
						.uri(path)
						.GET()
						.build();
			HttpResponse<String> res = client.send(req, BodyHandlers.ofString());
			return res.body();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@SuppressWarnings("resource")
	public static double askCurrencyToUser(Map<String, String> currencies) {
		Scanner scan = new Scanner(System.in);
		String userSelection;
		userSelection = scan.nextLine();
		String currency = currencies.get(userSelection.toUpperCase());
		while (currency == null) {
			System.out.println("That's not in the selection, select another one");
			userSelection = scan.nextLine();
			currency = currencies.get(userSelection.toUpperCase());
		}
		return Double.parseDouble(currency);
	}

	public static void main(String[] args) {
		SpringApplication.run(ConversorApplication.class, args);

		Scanner scan = new Scanner(System.in);
		String jsonResponse = "";

		URI url = getRelativePath("latest/USD");

		jsonResponse = requestCurrencies(url);

		String conversions = getConversionRates(jsonResponse);
		Map<String, String> currencies = convertJson(conversions);

		printCurrencies(currencies);

		System.out.println("Escribe la moneda a convertir en el formato de 3 letras que indica la lista.");
		double base = askCurrencyToUser(currencies);

		System.out.println("Ingresa la cantidad a convertir");
		double money = scan.nextDouble();
		scan.nextLine(); // Clear the buffer

		System.out.println("Escribe la moneda a la que deseas convertir el valor en el formato de 3 letras que indica la lista.");
		String targetCurrencyCode = scan.nextLine().toUpperCase(); 
		double target = Double.parseDouble(currencies.get(targetCurrencyCode)); 

		double conversion = (money / base) * target;

		DecimalFormat df = new DecimalFormat("#.00");
		System.out.println(df.format(conversion) + " " + targetCurrencyCode + " es el resultado obtenido.");
	}
}


