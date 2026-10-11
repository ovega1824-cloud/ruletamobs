package com.mobsruleta.client;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;

/**
 * Lee los modelos de los mobs desde assets/mobsruleta/mobdefs/mobs.json (el mismo archivo con el que se pintaron
 * las texturas, así los UV siempre coinciden).
 */
public final class ModelDefs {

    private static JsonObject raiz;

    private ModelDefs() {
    }

    private static synchronized JsonObject raiz() {
        if (raiz == null) {
            try (InputStream in = ModelDefs.class.getResourceAsStream("/assets/mobsruleta/mobdefs/mobs.json")) {
                raiz = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            } catch (Exception e) {
                throw new IllegalStateException("No se pudo leer mobs.json", e);
            }
        }
        return raiz;
    }

    public static TexturedModelData datos(String id) {
        JsonObject mob = raiz().getAsJsonObject(id);
        ModelData datos = new ModelData();
        ModelPartData base = datos.getRoot();
        for (JsonElement el : mob.getAsJsonArray("parts")) {
            JsonObject p = el.getAsJsonObject();
            JsonArray uv = p.getAsJsonArray("uv");
            JsonArray origen = p.getAsJsonArray("origin");
            JsonArray tam = p.getAsJsonArray("size");
            JsonArray pivote = p.getAsJsonArray("pivot");
            base.addChild(p.get("name").getAsString(),
                    ModelPartBuilder.create()
                            .uv(uv.get(0).getAsInt(), uv.get(1).getAsInt())
                            .cuboid((float) origen.get(0).getAsDouble(), (float) origen.get(1).getAsDouble(), (float) origen.get(2).getAsDouble(),
                                    (float) tam.get(0).getAsDouble(), (float) tam.get(1).getAsDouble(), (float) tam.get(2).getAsDouble()),
                    ModelTransform.pivot((float) pivote.get(0).getAsDouble(), (float) pivote.get(1).getAsDouble(), (float) pivote.get(2).getAsDouble()));
        }
        return TexturedModelData.of(datos, mob.get("tw").getAsInt(), mob.get("th").getAsInt());
    }

    /** nombre de la parte -> rol de animación (head, leg0, leg1, arm0, arm1, wing0, wing1, sway, bob, static). */
    public static Map<String, String> roles(String id) {
        Map<String, String> roles = new LinkedHashMap<>();
        for (JsonElement el : raiz().getAsJsonObject(id).getAsJsonArray("parts")) {
            JsonObject p = el.getAsJsonObject();
            roles.put(p.get("name").getAsString(), p.get("role").getAsString());
        }
        return roles;
    }
}
