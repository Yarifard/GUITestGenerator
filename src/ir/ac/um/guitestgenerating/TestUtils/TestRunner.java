package ir.ac.um.guitestgenerating.TestUtils;

import ir.ac.um.guitestgenerating.ModelConstructor.FeatureModel.EventHandlerInformation;
import ir.ac.um.guitestgenerating.TestUtils.StreamGobbler;
import ir.ac.um.guitestgenerating.Util.Utils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TestRunner {

    public static boolean isBuildTest(String path){
        boolean flag = true;
        try {

            Process process;
            //String[] command = {"cmd /c shellCommand"};
            Runtime runtime = Runtime.getRuntime();
            process =
                    runtime.exec("cmd /c gradlew compileDebugAndroidTestSources",
                            null, new File(path));
            StreamGobbler streamGobbler =
                    new StreamGobbler(process.getInputStream(), System.out::println);
            Future<?> future = Executors.newSingleThreadExecutor().submit(streamGobbler);

            int exitCode = process.waitFor();
            assert exitCode == 0;

            future.get(); // waits for streamGobbler to finish

        } catch (InterruptedException | ExecutionException | IOException e) {
            e.printStackTrace();
            flag = false;
        }
        return flag;
    }

    public static boolean isBuildTestsAndInstalledAppSuccess(String path){
        boolean result = false;

        try {

            Process process;
            //String[] command = {"cmd /c shellCommand"};
            Runtime runtime = Runtime.getRuntime();
            process =
                    runtime.exec("cmd /c gradlew installDebug compileDebugAndroidTestSources  installDebugAndroidTest",
                            null, new File(path));
            StreamGobbler streamGobbler =
                    new StreamGobbler(process.getInputStream(), System.out::println);
            Future<?> future = Executors.newSingleThreadExecutor().submit(streamGobbler);

            int exitCode = process.waitFor();
            assert exitCode == 0;

            future.get(); // waits for streamGobbler to finish
            result = true;

        }catch (IOException | InterruptedException | ExecutionException ioException) {
            Utils.showMessage(ioException.getMessage());
            result = false;
        }
        return result;
    }


    public static boolean isInstalledAppSuccess(String path){
        boolean result = false;
       // String appPath = projectInformation.getBaseDirectory().getCanonicalPath();
        try {

            Process process;
            //String[] command = {"cmd /c shellCommand"};
            Runtime runtime = Runtime.getRuntime();
            process =
                    runtime.exec("cmd /c gradlew installDebug installDebugAndroidTest", null, new File(path));
            StreamGobbler streamGobbler =
                    new StreamGobbler(process.getInputStream(), System.out::println);
            Future<?> future = Executors.newSingleThreadExecutor().submit(streamGobbler);

            int exitCode = process.waitFor();
            assert exitCode == 0;

            future.get(); // waits for streamGobbler to finish
            result = true;

        }catch (IOException | InterruptedException | ExecutionException ioException) {
                Utils.showMessage(ioException.getMessage());
                result = false;
        }
        return result;
    }


    public static boolean isUninstalledAppSuccess(String path, String packageName){
        boolean result = false;
//        String packageName = projectInformation.getPackageName();
//        String appPath = projectInformation.getBaseDirectory().getCanonicalPath();
        try {

            Process process;
            Runtime runtime = Runtime.getRuntime();
            process =
                    runtime.exec("cmd /c adb uninstall " + packageName, null, new File(path));
            StreamGobbler streamGobbler =
                    new StreamGobbler(process.getInputStream(), System.out::println);
            Future<?> future = Executors.newSingleThreadExecutor().submit(streamGobbler);

            int exitCode = process.waitFor();
            assert exitCode == 0;

            future.get(); // waits for streamGobbler to finish
            result = true;

        }catch (IOException | InterruptedException | ExecutionException ioException) {
            Utils.showMessage(ioException.getMessage());
            result = false;
        }
        return result;
    }

    public static List<String> listFilesUsingDirectoryStream(String dir) {
        File maindir = new File((java.lang.String) dir);
        List<String> fileNames = new ArrayList<>();
        if (maindir.exists() && maindir.isDirectory()) {
            File[] files = maindir.listFiles();
            Arrays.sort(files, Comparator.comparingLong(File::lastModified));
            for(File file : files)
                if(!file.isDirectory())
                    fileNames.add((String) file.getName().substring(0,file.getName().lastIndexOf('.')));
        }
        return fileNames;
    }


    public  static boolean isExecuable(String path,String packageName,String testFilesPath){
        boolean result = false;
        try {
            Process process;
            //String[] command = {"cmd /c shellCommand"};
            Runtime runtime = Runtime.getRuntime();
            process =
                    runtime.exec("cmd /c CompileTestCaseAndInstallApp", null, new File(path));
            StreamGobbler streamGobbler =
                    new StreamGobbler(process.getInputStream(), System.out::println);
            Future<?> future = Executors.newSingleThreadExecutor().submit(streamGobbler);

            int exitCode = process.waitFor();
            assert exitCode == 0;

            future.get(); // waits for streamGobbler to finish

            //-------------------------------------------------
            List<String> testClassFileNames = listFilesUsingDirectoryStream(testFilesPath);
            for(String testFileName:testClassFileNames){
                String command = "cmd /c " + "adb shell am instrument -w -r -e debug false -e class '" +
                        packageName + "." + testFileName +"' " + packageName +".test/androidx.test.runner.AndroidJUnitRunner";
                process =
                        runtime.exec(command, null, new File(path));

                streamGobbler =
                        new StreamGobbler(process.getInputStream(), System.out::println);
                future = Executors.newSingleThreadExecutor().submit(streamGobbler);

                exitCode = process.waitFor();
                assert exitCode == 0;

                future.get(); // waits for streamGobbler to finish
                if(streamGobbler.getStatus()){
                    Utils.showMessage("Test executed successfully");
                    result = true;
                }
                else{
                    Utils.showMessage("Test Failed");
                    result = false;
                    break;
                }
            }

            File directory = new File(testFilesPath);
            FileUtils.cleanDirectory(directory);
            isUninstalledAppSuccess(path,packageName);
        } catch (IOException | InterruptedException | ExecutionException ioException) {
            Utils.showMessage(ioException.getMessage());
        }
        System.gc();
        return result;
    }

    public static boolean isExecuable(String path, String packageName,EventHandlerInformation event){
        boolean result = false;
//        String packageName = projectInformation.getPackageName();
//        String appPath = projectInformation.getBaseDirectory().getCanonicalPath();
        try {
//
//            Process process;
//            //String[] command = {"cmd /c shellCommand"};
            Runtime runtime = Runtime.getRuntime();
//            process =
//                    runtime.exec("cmd /c gradlew installDebug installDebugAndroidTest", null, new File("D://AndroidExample//Contacts//"));
//            StreamGobbler streamGobbler =
//                    new StreamGobbler(process.getInputStream(), System.out::println);
//            Future<?> future = Executors.newSingleThreadExecutor().submit(streamGobbler);
//
//            int exitCode = process.waitFor();
//            assert exitCode == 0;
//
//            future.get(); // waits for streamGobbler to finish

            //-------------------------------------------------
            String command = "cmd /c ExecuteTestCaseAndUnInstallApp " + packageName + "." +
                    event.getSourceActivity() +"Test#" + event.getTitle()+" " + packageName;
            Process process =
                       runtime.exec(command, null, new File(path));

            StreamGobbler  streamGobbler =
                                          new StreamGobbler(process.getInputStream(), System.out::println);
            Future<?>  future = Executors.newSingleThreadExecutor().submit(streamGobbler);

            int exitCode = process.waitFor();
                assert exitCode == 0;

            future.get(); // waits for streamGobbler to finish
            if(streamGobbler.getStatus()){
                    Utils.showMessage("Test executed successfully");
                    result = true;
            }
            else{
                    Utils.showMessage("Test Failed");
                    result = false;
            }
            isUninstalledAppSuccess(path,packageName);
        } catch (IOException | InterruptedException | ExecutionException ioException) {
            Utils.showMessage(ioException.getMessage());
        }
        return result;
    }

    public static boolean isExecutable(String path, String packageName,String appPackage, EventHandlerInformation event){
        boolean result = false;
//        String packageName = projectInformation.getPackageName();
//        String appPath = projectInformation.getBaseDirectory().getCanonicalPath();
        //path = path.replaceAll("/","//") + "//";
        try {
//
//            Process process;
//            //String[] command = {"cmd /c shellCommand"};
            Runtime runtime = Runtime.getRuntime();
//            process =
//                    runtime.exec("cmd /c gradlew installDebug installDebugAndroidTest", null, new File("D://AndroidExample//Contacts//"));
//            StreamGobbler streamGobbler =
//                    new StreamGobbler(process.getInputStream(), System.out::println);
//            Future<?> future = Executors.newSingleThreadExecutor().submit(streamGobbler);
//
//            int exitCode = process.waitFor();
//            assert exitCode == 0;
//
//            future.get(); // waits for streamGobbler to finish

            //-------------------------------------------------
            String command = "cmd /c ExecuteTestCase " + packageName + "." +
                    event.getSourceActivity() +"Test#" + event.getTitle() + " " + appPackage;
            Process process =
                    runtime.exec(command, null, new File(path));

            StreamGobbler  streamGobbler =
                    new StreamGobbler(process.getInputStream(), System.out::println);
            Future<?>  future = Executors.newSingleThreadExecutor().submit(streamGobbler);

            int exitCode = process.waitFor();
            assert exitCode == 0;

            future.get(); // waits for streamGobbler to finish
            if(streamGobbler.getStatus()){
                Utils.showMessage(event.getSourceActivity() + "." + event.getTitle() +  ":successfully Running");
                result = true;
            }
            else{
                Utils.showMessage(event.getSourceActivity() + "." + event.getTitle() +  ":Failed Running");
                result = false;
            }

        } catch (IOException | InterruptedException | ExecutionException ioException) {
            Utils.showMessage(ioException.getMessage());
        }
        return result;
    }

    public static boolean isExecutableWithTails(String path, String packageName,EventHandlerInformation event) {
        boolean result = true;
        if (event.getDependentEventHandlersList().isEmpty())
            return true;
        else
            for (EventHandlerInformation eventItem : event.getDependentEventHandlersList()) {
                if (isExecutableWithTails(path, packageName, eventItem)) {
                    try {
                        Process process;
                        Runtime runtime = Runtime.getRuntime();
                        String command = "cmd /c ExecuteTestCase " + packageName + "." +
                                eventItem.getSourceActivity() + "Test#" + eventItem.getTitle() + " " + packageName;
                        process =
                                runtime.exec(command, null, new File(path));

                        StreamGobbler streamGobbler =
                                new StreamGobbler(process.getInputStream(), System.out::println);
                        Future<?> future = Executors.newSingleThreadExecutor().submit(streamGobbler);

                        int exitCode = process.waitFor();
                        assert exitCode == 0;

                        future.get(); // waits for streamGobbler to finish
                        if (!streamGobbler.getStatus()) {
                            result = false;
                            break;
                        }
                    } catch (IOException | InterruptedException | ExecutionException ioException) {
                        Utils.showMessage(ioException.getMessage());
                    }
                }
            }
        return result;
    }

    public static boolean isExecutableEvaluationTestClass(String path,String packageName) {
        boolean result = false;
//        String  packageName = projectInformation.getPackageName();
//        String  appPath = projectInformation.getBaseDirectory().getCanonicalPath();
        try {
                Process process;
                //String[] command = {"cmd /c shellCommand"};
                Runtime runtime = Runtime.getRuntime();
                process =
                        runtime.exec("cmd /c gradlew compileDebugAndroidTestSources installDebugAndroidTest ",
                                null, new File(path));
                StreamGobbler streamGobbler =
                        new StreamGobbler(process.getInputStream(), System.out::println);
                Future<?> future = Executors.newSingleThreadExecutor().submit(streamGobbler);

                int exitCode = process.waitFor();
                assert exitCode == 0;

                future.get(); // waits for streamGobbler to finish

                //-------------------------------------------------
                String command = "cmd /c " + "adb shell am instrument -w -r -e debug false -e class '" +
                            packageName + ".EvaluationTest" + "' " + packageName + ".test/androidx.test.runner.AndroidJUnitRunner";
                process =
                            runtime.exec(command, null, new File(path));
                streamGobbler =
                            new StreamGobbler(process.getInputStream(), System.out::println);
                future = Executors.newSingleThreadExecutor().submit(streamGobbler);

                exitCode = process.waitFor();
                assert exitCode == 0;

                future.get(); // waits for streamGobbler to finish
                if(streamGobbler.getStatus()){
                    Utils.showMessage("Test executed successfully");
                    result = true;
                }
                else{
                     Utils.showMessage("Test Failed");
                     result = false;
                }
            } catch (InterruptedException | ExecutionException | IOException e) {
            e.printStackTrace();
            }
        System.gc();
        return result;
    }
}
