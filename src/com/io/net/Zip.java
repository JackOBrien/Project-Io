package com.io.net;

/**
 * Used from mkyong from his site and post
 * http://www.mkyong.com/java/how-to-compress-files-in-zip-format/
 *
 * There have been modifications tot his file
 */

import java.io.*;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zip
{
    public static byte[] zip(String sourceFolder) throws IOException {

        byte[] buffer = new byte[1024];
        ArrayList<String> fileList = new ArrayList<String>();

        generateFileList(sourceFolder, new File(sourceFolder), fileList);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(bos);

        System.out.println("Output to byte array");

        for (String file : fileList) {
            if (file.contains(".git")) {
                continue;
            }

            System.out.println("File Added : " + file);
            ZipEntry ze = new ZipEntry(file);
            zos.putNextEntry(ze);

            FileInputStream in = new FileInputStream(sourceFolder + File.separator + file);

            int len;
            while ((len = in.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }

            in.close();
        }

        zos.closeEntry();
        zos.close();

        System.out.println("Done Zipping");

        return bos.toByteArray();
    }

    private static void generateFileList(String sourceFolder, File node, ArrayList<String> fileList) {

        if (node.isFile()) {
            fileList.add(generateZipEntry(sourceFolder, node.getAbsoluteFile().toString()));
        }

        if (node.isDirectory()) {
            String[] subNode = node.list();
            for (String filename : subNode) {
                generateFileList(sourceFolder, new File(node, filename), fileList);
            }
        }

    }

    private static String generateZipEntry(String sourceFolder, String file) {
        return file.substring(sourceFolder.length()+1, file.length());
    }
}
