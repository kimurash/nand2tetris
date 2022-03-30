import java.io.File;

public class JackAnalyzer {
    public static void main(String[] args) {
        CompilationEngine engine = null;
        
        File path = new File(args[0]);
        if(path.isDirectory()){
            File[] fileList = path.listFiles();
            if(fileList == null){
                System.out.println("No files exist.");
            } else{
                for(int i=0; i < fileList.length; i++){
                    if(fileList[i].isFile()){
                        String filename = fileList[i].getName();
                        String ext = filename.substring(filename.lastIndexOf("."));
                        if(ext.equals(".jack")){
                            engine = new CompilationEngine(fileList[i]);
                            engine.compileClass();
                            engine.closeFile();
                        }
                    }
                }
            }
        } else if(path.isFile()){
            engine = new CompilationEngine(path);
            engine.compileClass();
            engine.closeFile();
        } else{
            System.out.println("Can't find such directory or file.");
        }
    }
}
