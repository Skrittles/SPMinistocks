/*
 The MIT License

 Copyright (c) 2013 Nitesh Patel http://niteshpatel.github.io/ministocks

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */

package nitezh.ministock;

import android.app.Activity;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import nitezh.ministock.domain.AndroidWidgetRepository;
import nitezh.ministock.domain.PortfolioStockRepository;
import nitezh.ministock.domain.Widget;
import nitezh.ministock.domain.WidgetRepository;


public class UserData {

    // Object for intrinsic lock
    public static final Object sFileBackupLock = new Object();
    private boolean OVERWRITE_BACKUP = false;

    public static void cleanupPreferenceFiles(Context context) {
        ArrayList<String> preferencesPathsInUse = getPreferencesPathsInUse(context);
        String sharedPrefsPath = context.getFilesDir().getParentFile().getPath() + "/shared_prefs";
        removeFilesExceptWhitelist(sharedPrefsPath, preferencesPathsInUse);
    }

    private static void removeFilesExceptWhitelist(String sharedPrefsFolder, ArrayList<String> preferencesFilenames) {
        File sharedPrefsDir = new File(sharedPrefsFolder);
        if (sharedPrefsDir.exists()) {
            for (File f : sharedPrefsDir.listFiles()) {
                if (!preferencesFilenames.contains(f.getName())) {
                    f.delete();
                }
            }
        }
    }

    private static ArrayList<String> getPreferencesPathsInUse(Context context) {
        ArrayList<String> filenames = new ArrayList<>();

        filenames.add(context.getString(R.string.prefs_name) + ".xml");
        for (int id : new AndroidWidgetRepository(context).getIds()) {
            filenames.add(context.getString(R.string.prefs_name) + id + ".xml");
        }

        return filenames;
    }

    public static void backupWidget(Context context, int appWidgetId, String backupName) {
        try {

            JSONObject jsonForAllWidgets = getJsonBackupsForAllWidgets(context);

            JSONObject jsonForWidget = getJsonForWidget(context, appWidgetId);

            jsonForAllWidgets.put(backupName, jsonForWidget);
            setJsonForAllWidgets(context, jsonForAllWidgets);
        } catch (JSONException ignored) {
        }
    }

    private static void setJsonForAllWidgets(Context context, JSONObject jsonForAllWidgets) {
        writeInternalStorage(context, jsonForAllWidgets.toString(), PortfolioStockRepository.WIDGET_JSON);
    }

    private static JSONObject getJsonForWidget(Context context, int appWidgetId) {
        WidgetRepository widgetRepository = new AndroidWidgetRepository(context);
        return widgetRepository.getWidget(appWidgetId).getWidgetPreferencesAsJson();
    }

    private static JSONObject getJsonBackupsForAllWidgets(Context context) throws JSONException {
        JSONObject backupContainer = new JSONObject();
        String rawJson = readInternalStorage(context, PortfolioStockRepository.WIDGET_JSON);
        if (rawJson != null) {
            backupContainer = new JSONObject(rawJson);
        }

        return backupContainer;
    }

    public static void restoreWidget(Context context, int appWidgetId, String backupName) {
        try {
            JSONObject jsonBackupsForAllWidgets = getJsonBackupsForAllWidgets(context);

            Widget widget = new AndroidWidgetRepository(context).getWidget(appWidgetId);
            widget.setWidgetPreferencesFromJson(
                    jsonBackupsForAllWidgets.getJSONObject(backupName));

            InformUserWidgetBackupRestored(context);
            ReloadPreferences((Activity) context);
        } catch (JSONException ignored) {
        }
    }

    private static void InformUserWidgetBackupRestored(Context context) {
        DialogTools.showSimpleDialog(context, "AppWidgetProvider restored",
                "The current widget preferences have been restored from your selected backup.");
    }

    private static void ReloadPreferences(Activity activity) {
        Intent intent = activity.getIntent();
        activity.finish();
        activity.startActivity(intent);
    }

    /*
    public static CharSequence[] getWidgetBackupNames(Context context) {
        try {
            String rawJson = readInternalStorage(context, PortfolioStockRepository.WIDGET_JSON);
            if (rawJson == null) {
                return null;
            }

            JSONObject jsonBackupsForAllWidgets = getJsonBackupsForAllWidgets(context);
            Iterator iterator = jsonBackupsForAllWidgets.keys();
            ArrayList<String> backupList = new ArrayList<>();
            while (iterator.hasNext()) {
                backupList.add((String) iterator.next());
            }

            return backupList.toArray(new String[backupList.size()]);
        } catch (JSONException ignored) {
        }

        return null;
    }
    */


    /**
     * Read all file names from an internal directory of folder ministocks in external storage.
     * Could be an SD-Card or part of the formatted internal storage.
     * Just read file names, if permission with synchronized.
     * Created similar to getWidgetBackupNames(...).
     *
     *  @param context is not used, could be helpful, if you want to use JSonObjects.
     *  @param internalDirectory specifies the direcory where file names should be read.
     *
     *   @return Array of file names from internal directory (could be empty).
     *
     */

    public static CharSequence[] readFileNames(Context context, String internalDirectory) {

        File root = Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + "/ministocks/" + internalDirectory);

        // Create missing directories, if missing
        if(!dir.exists())
            dir.mkdirs();


        synchronized (UserData.sFileBackupLock) {
            File[] files = dir.listFiles();


            ArrayList<String> fileNames = new ArrayList<>();

            // list every file names in a list
            for (File file : files) {
                if (file.isFile())
                    fileNames.add(file.getName());
            }

            return fileNames.toArray(new String[fileNames.size()]);

        }
    }


    public static void writeInternalStorage(Context context, String stringData, String filename) {
        try {
            synchronized (UserData.sFileBackupLock) {
                FileOutputStream fos;
                fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
                fos.write(stringData.getBytes());
                fos.close();
            }
            new BackupManager(context).dataChanged();
        } catch (IOException ignored) {
        }
    }

    public static String readInternalStorage(Context context, String filename) {
        try {
            StringBuffer fileContent = new StringBuffer();

            synchronized (UserData.sFileBackupLock) {
                FileInputStream fis;
                fis = context.openFileInput(filename);
                byte[] buffer = new byte[1024];
                while (fis.read(buffer) != -1) {
                    fileContent.append(new String(buffer));
                }
            }

            return new String(fileContent);
        } catch (IOException ignored) {
        }

        return null;
    }


    /**
     *  read a specific file from external Storage with FileInputStream.
     *  Specific folder name inside of externalStorage/ministocks/
     *  external Storage could be on SD-card or on formatted internal storage.
     *
     * @param context not used, because method was created similar to readInternalStorage(...)
     * @param fileName specifies the file name which should be read.
     * @param internalDirectory specifies the directory in ministocks from which a file should be read
     * @return file content as String.
     */
    public static String readExternalStorage(Context context, String fileName, String internalDirectory) {

        File root = Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + "/ministocks/" + internalDirectory);

        // Create missing directories, if required.
        if(!dir.exists())
            dir.mkdirs();

        File file = new File(dir, fileName);

        try {
            StringBuffer fileContent = new StringBuffer();
            String tmp;

            // lock file for other operations.
            synchronized (UserData.sFileBackupLock) {
                FileInputStream fis = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(fis);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                while ((tmp = bufferedReader.readLine()) != null) {
                    fileContent.append(tmp + "\n");
                }
            }

            String res = new String(fileContent);

            return res;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
            //DialogTools.showSimpleDialog(context, "Restore portfolio failed", "Backup file portfolioJson.txt not found");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // return null, if file was not found, or other errors happend.
        return null;
    }

    /**
     *  Create a new directory and a new file inside of externalStorage/ministocks/ , where
     *  your data will be written inside of a new file.
     *  If external Storage is not available, print a message for User.
     *
     * @param context Print an error for user if external Storage is not available.
     * @param data String, that should be written to your file.
     * @param fileName names the file you will write to in externalStorage/ministocks/internalDirectory/
     * @param internalDirectory names directory inside of ministocks/
     */

    public static void writeExternalStorage(Context context, String data, String fileName, String internalDirectory) {

        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {

            File root = Environment.getExternalStorageDirectory();
            File dir = new File(root.getAbsolutePath() + "/ministocks/" + internalDirectory);

            // Create missing directories, if required.
            if(!dir.exists()) {
                if (dir.mkdirs()) {
                    Log.d("Fcreate","Folder created" +"\n" + dir);
                } else {
                    Log.d("Ffailed", "Creating folder failed\n" + dir);
                }
            } else {
                Log.d("Fexists", dir + " already exists");
            }

            if (new File(dir, fileName).exists()) {
                DialogTools.showSimpleDialog(context, "Warning", "Choose a diffrent name for your backup. This one already exists");
            }
            new File(dir, fileName).delete();

            File file = new File(dir, fileName);


            try {
                synchronized (UserData.sFileBackupLock) {
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(data.getBytes());
                    fos.close();

                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {

            DialogTools.showSimpleDialog(context, "External Storage not available",
                    "Your external Storage is nor available for this application ");

        }
    }
}
