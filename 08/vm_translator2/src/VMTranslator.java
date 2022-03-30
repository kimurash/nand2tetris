import java.io.File;

public class VMTranslator{
    public void translateFile(CodeWriter writer, File file){
        Parser parser = new Parser(file.getAbsolutePath());

        while(parser.hasMoreCommands()){
            int id = parser.commandType().getId();
            switch(id){
                case 0 : // C_ARITHMETIC
                    writer.writeArithmetic(parser.arg1());
                    break;
                case 1 : // C_PUSH
                    writer.writePushPop(CommandType.C_PUSH, parser.arg1(), parser.arg2());
                    break;
                case 2 : // C_POP
                    writer.writePushPop(CommandType.C_POP, parser.arg1(), parser.arg2());
                    break;
                case 3 : // C_LABEL
                    writer.writeLabel(parser.arg1());
                    break;
                case 4 :  // C_GOTO
                    writer.writeGoto(parser.arg1());
                    break;
                case 5 : // C_IF
                    writer.writeIf(parser.arg1());
                    break;
                case 6 : // C_FUNCTION
                    writer.writeFunction(parser.arg1(), parser.arg2());
                    break;
                case 7 : // C_RETURN
                    writer.writeReturn();
                    break;
                case 8 : // C_CALL
                    writer.writeCall(parser.arg1(), parser.arg2());
                    break;
                default : break;
            }
            System.out.println();
        }

        parser.closeFile();
    }

    public static void main(String[] args) {
        VMTranslator translator = new VMTranslator();

        File path = new File(args[0]);
        CodeWriter writer = new CodeWriter(path);
        if(path.isDirectory()){
            File[] fileList = path.listFiles();
            if(fileList == null){
                System.out.println("No files exist.");
            } else{
                for(int i=0; i < fileList.length; i++){
                    // writer = new CodeWriter(fileList[i]);
                    String filename = fileList[i].getName();
                    String ext = filename.substring(filename.lastIndexOf("."));
                    if(ext.equals(".vm")){
                        writer.setFileName(filename.substring(0, filename.lastIndexOf(".")));
                        translator.translateFile(writer, fileList[i]);
                    }
                }
            }
        } else if(path.isFile()){
            // writer = new CodeWriter(path);
            translator.translateFile(writer, path);
        } else{
            System.out.println("Can't find such directory or file.");
        }
        writer.closeFile();
    }
}