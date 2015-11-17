package com.io.net;

/**
 * Used from mkyong from his site and post
 * http://www.mkyong.com/java/how-to-decompress-files-from-a-zip-file/
 *
 * There have been modifications tot his file
 */

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UnZip
{
    List<String> fileList;
    private byte[] inputBytes;
    private String outFolder;

    public static void main( String[] args )
    {
        String inputFileDir = "/home/vi1i/git/Project-Io/test.zip";

        Path inputFile = Paths.get(inputFileDir);

        byte[] content;

        try {
            content = Files.readAllBytes(inputFile);
        }
        catch (IOException ex) {
            System.out.println("Failed to read test.zip");
            return;
        }

        String outFolder = "/home/vi1i/git/Project-Io/unzipTest";
        UnZip unZip = new UnZip(content, outFolder);
        unZip.unZipIt();
    }

    /**
     * Unzip it
     * @param inputBytes input byte array
     * @param outFolder zip file output folder
     */
    public UnZip(byte[] inputBytes, String outFolder){
        this.inputBytes = inputBytes;
        this.outFolder = outFolder;
    }
    public void unZipIt(){

        byte[] buffer = new byte[1024];

        try{

            //create output directory is not exists
            File folder = new File(this.outFolder);
            if(!folder.exists()){
                folder.mkdir();
            }

            //create input zip stream from byte array
            ByteArrayInputStream byteStream = new ByteArrayInputStream(this.inputBytes);
            ZipInputStream zis = new ZipInputStream(byteStream);

            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while(ze!=null){

                String fileName = ze.getName();
                File newFile = new File(this.outFolder + File.separator + fileName);

                System.out.println("file unzip : "+ newFile.getAbsoluteFile());

                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();

            System.out.println("Done");

        }catch(IOException ex){
            ex.printStackTrace();
        }
    }
}