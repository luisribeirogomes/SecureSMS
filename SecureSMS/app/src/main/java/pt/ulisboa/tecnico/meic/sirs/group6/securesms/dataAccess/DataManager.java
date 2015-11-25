package pt.ulisboa.tecnico.meic.sirs.group6.securesms.dataAccess;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import pt.ulisboa.tecnico.meic.sirs.group6.securesms.dataAccess.exceptions.FailedToCreateDataBaseException;
import pt.ulisboa.tecnico.meic.sirs.group6.securesms.dataAccess.exceptions.FailedToLoadDataBaseException;
import pt.ulisboa.tecnico.meic.sirs.group6.securesms.dataAccess.exceptions.FailedToRetrieveDataException;

public class DataManager {

    private static SharedPreferences sharedPreferences = null;
    private static DataManager dataManager = new DataManager();

    public static DataManager getInstance() throws FailedToLoadDataBaseException {
        if(sharedPreferences == null) {
            throw new FailedToLoadDataBaseException();
        }
        return dataManager;
    }

    public static DataManager getInstance(Context context) throws FailedToCreateDataBaseException {
        if(sharedPreferences != null) {
            throw new FailedToCreateDataBaseException();
        }
        sharedPreferences = context.getSharedPreferences("userDetails", 0);
        return dataManager;
    }

    public DataManager() {

    }

    public void add(String name, String object) {
        Editor editor;

        Set<String> dataSet = sharedPreferences.getStringSet(name, new TreeSet<String>());
        List<String> dataList = new ArrayList<String>(dataSet);
        dataList.add(object);
        dataSet = new TreeSet<String>(dataList);

        editor = sharedPreferences.edit();
        editor.putStringSet(name, dataSet);
        editor.commit();
    }

    public void remove(String name, String object) {
        Editor editor;

        Set<String> dataSet = sharedPreferences.getStringSet(name, new TreeSet<String>());
        dataSet.remove(object);

        editor = sharedPreferences.edit();
        editor.putStringSet(name, dataSet);
        editor.commit();
    }

    public Set<String> getAll(String name) throws FailedToRetrieveDataException {
        Set<String> stringSet = sharedPreferences.getStringSet(name, new TreeSet<String>());

        return stringSet;
    }
}