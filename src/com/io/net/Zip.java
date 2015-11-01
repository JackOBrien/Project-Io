package com.io.net;

/**
 * Used from mkyong from his site and post
 * http://www.mkyong.com/java/how-to-compress-files-in-zip-format/
 *
 * There have been modifications tot his file
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zip
{
    List<String> fileList;
    private String outFile;
    private String sourceFolder;

    public Zip(String sourceFolder, String outFile){
        fileList = new ArrayList<String>();
        this.sourceFolder = sourceFolder;
        this.outFile = outFile;
    }

    public static void main(String args[]) {
        String sourceFolder = "/home/vi1i/git/Project-Io";
        String outFile = "/home/vi1i/git/Project-Io/test.zip";

        Zip zip = new Zip(sourceFolder, outFile);
        zip.generateFileList(new File(sourceFolder));
        zip.zipIt(outFile);
    }

    /**
     * Zip it
     * @param zipFile output ZIP file location
     */
    public void zipIt(String zipFile){

        byte[] buffer = new byte[1024];

        try{

            FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = new ZipOutputStream(fos);

            System.out.println("Output to Zip : " + zipFile);

            for(String file : this.fileList){

                System.out.println("File Added : " + file);
                ZipEntry ze= new ZipEntry(file);
                zos.putNextEntry(ze);

                FileInputStream in =
                        new FileInputStream(this.sourceFolder + File.separator + file);

                int len;
                while ((len = in.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }

                in.close();
            }

            zos.closeEntry();
            //remember close it
            zos.close();

            System.out.println("Done");
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    /**
     * Traverse a directory and get all files,
     * and add the file into fileList
     * @param node file or directory
     */
    public void generateFileList(File node){

        //add file only
        if(node.isFile()){
            fileList.add(generateZipEntry(node.getAbsoluteFile().toString()));
        }

        if(node.isDirectory()){
            String[] subNote = node.list();
            for(String filename : subNote){
                generateFileList(new File(node, filename));
            }
        }

    }

    /**
     * Format the file path for zip
     * @param file file path
     * @return Formatted file path
     */
    private String generateZipEntry(String file){
        return file.substring(this.sourceFolder.length()+1, file.length());
    }
}
