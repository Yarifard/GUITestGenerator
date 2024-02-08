package ir.ac.um.guitestgenerating.Project;

import java.util.HashMap;
import java.util.Map;

public class MapCollection {
    private int keyValue;
    private Map<Integer,String> map;

    public MapCollection(){
        keyValue = 0;
        map = new HashMap<>();
    }

    public String getLabel(int keyValue){
        String label = "";
        if(map.containsKey(keyValue))
          label = map.get(keyValue);
        return label;
    }

    public boolean isEmpty(){
        return map.isEmpty();
    }

    public int getSize(){
        return  map.size();
    }

    public boolean putLabel(String label){
        keyValue++;
        map.put(keyValue,label);
        if(map.get(keyValue).contentEquals(label))
            return true;
        else
            return false;

    }
    public int getKeyValue(){
        return keyValue;
    }

    public boolean replaceLabel(int key,String label){
        map.replace(key,label);
        if(map.get(key).contentEquals(label))
            return true;
        else
            return false;
    }

    public String remove(int keyValue){
       return map.remove(keyValue);
    }



}
