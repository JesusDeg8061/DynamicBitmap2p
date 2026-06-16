package com.dynamicbitmap.core;

import com.dynamicbitmap.node.Node;
import com.dynamicbitmap.ui.MainUI;

import java.util.HashMap;
import java.util.Map;

public class AppContext {
    
    public static MainUI backend;
    public static Node node;

    public static Map<String,Integer> myFiles =
            new HashMap<>();

    public static Map<String,Long> fileSizes =
            new HashMap<>();

    public static Map<String,String> fileIds =
            new HashMap<>();

    public static Map<String,byte[]> fileKeys =
            new HashMap<>();
}