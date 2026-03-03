package com.example.hotelio.services;



import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

public class GeminiService {

    private static final String API_KEY = "AIzaSyCSw_jsVPS77Wm1OZ0nKACW81CwLtYckfY";
    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;

    private final OkHttpClient client;
    private final Gson gson;
    private StringBuilder conversationHistory;

    public GeminiService() {

        this.client = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(15))
                .readTimeout(Duration.ofSeconds(30))
                .build();

        this.gson = new Gson();
        this.conversationHistory = new StringBuilder();
        initializeContext();
    }

    private void initializeContext() {
        conversationHistory.append("""
                Tu es un assistant virtuel pour HotelIO, un hôtel de luxe.
                Ton rôle est d'aider les clients à trouver la chambre parfaite.
                Sois chaleureux, professionnel et concis.

                Types de chambres :
                - SIMPLE: 1 personne, 80 DT/nuit
                - DOUBLE: 2 personnes, 120-130 DT/nuit
                - SUITE: 4 personnes, 250 DT/nuit
                - DELUXE: 2-3 personnes, 350+ DT/nuit

                Services: WiFi gratuit, petit-déjeuner, parking, piscine.
                """);
    }

    public String obtenirReponse(String questionUtilisateur) throws IOException {

        conversationHistory.append("\n\nClient: ").append(questionUtilisateur);
        String prompt = conversationHistory + "\n\nAssistant:";

        // ---------- JSON PROPRE ----------
        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", prompt);

        JsonArray partsArray = new JsonArray();
        partsArray.add(textPart);

        JsonObject contentObject = new JsonObject();
        contentObject.add("parts", partsArray);

        JsonArray contentsArray = new JsonArray();
        contentsArray.add(contentObject);

        JsonObject requestBody = new JsonObject();
        requestBody.add("contents", contentsArray);

        // config génération
        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.7);
        generationConfig.addProperty("maxOutputTokens", 500);

        requestBody.add("generationConfig", generationConfig);
        // ----------------------------------

        RequestBody body = RequestBody.create(
                requestBody.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {

            String responseBody = response.body().string();

            if (!response.isSuccessful()) {
                throw new IOException("Erreur API: "
                        + response.code()
                        + "\nRéponse: " + responseBody);
            }

            String reponseIA = extraireTexteReponse(responseBody);

            conversationHistory.append("\n\nAssistant: ").append(reponseIA);

            return reponseIA;
        }
    }

    private String extraireTexteReponse(String jsonResponse) {

        try {
            JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);

            JsonArray candidates = jsonObject.getAsJsonArray("candidates");

            if (candidates != null && candidates.size() > 0) {

                JsonObject content =
                        candidates.get(0).getAsJsonObject()
                                .getAsJsonObject("content");

                JsonArray parts =
                        content.getAsJsonArray("parts");

                if (parts != null && parts.size() > 0) {
                    return parts.get(0).getAsJsonObject()
                            .get("text").getAsString();
                }
            }

            return "Désolé, aucune réponse générée.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur lors du traitement de la réponse.";
        }
    }

    public void reinitialiserConversation() {
        conversationHistory = new StringBuilder();
        initializeContext();
    }
}