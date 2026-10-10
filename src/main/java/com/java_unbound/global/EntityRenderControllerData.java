package com.java_unbound.global;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//being used to send the active data to the render controller function so it knows which textures and geometries to render
public class EntityRenderControllerData {
    public String Identifier = "";
    public String Geometry = "";
    public List<String> Textures = new ArrayList<>();
    public Map<String, String> Materials = new HashMap<>();
}