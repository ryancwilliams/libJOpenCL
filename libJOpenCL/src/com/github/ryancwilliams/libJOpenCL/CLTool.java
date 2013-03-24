/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.ryancwilliams.libJOpenCL;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CLEvent;
import org.lwjgl.opencl.CLKernel;
import org.lwjgl.opencl.CLProgram;
import org.lwjgl.opencl.Util;

/**
 *
 * @author ryancwilliams
 */
public abstract class CLTool {

    private final CLSession session;
    private final CLKernel kernel;
    protected final String name;
    protected final String path;
    private IntBuffer errorBuffer;
    private PointerBuffer event;

    /**
     * Creates a CLTool
     * @param session the session to run this tool on. 
     */
    public CLTool(CLSession session) {
        this(session, "", "");
    }
    
    /**
     * Creates a CLTool
     * @param session the session to run this tool on. 
     * @param name the name of the kernel.
     * @param path the path of the kernel.
     */
    protected CLTool(CLSession session, String name, String path) {
        //Load varables
        this.session = session;
        this.name = name;
        this.path = path;
        
        //Create error buffer
        this.errorBuffer = BufferUtils.createIntBuffer(1);
        
        //Create space to store events
        this.event = BufferUtils.createPointerBuffer(1);
        
        //Create program
        CLProgram program = CL10.clCreateProgramWithSource(
                this.session.getContext(), this.path, this.errorBuffer);
        
        //Check for errors
        Util.checkCLError(this.errorBuffer.get(0));
        
        int error = CL10.clBuildProgram(program, this.session.getDevice(), "", null);
        
        //Check for errors
        Util.checkCLError(error);
        
        //Load the kernal
        this.kernel = CL10.clCreateKernel(program, this.name, this.errorBuffer);
        
        //Check for errors
        Util.checkCLError(this.errorBuffer.get(0));
        
    }
    
    /**
     * Runs the kernel
     * @param size the size of the vectors being computed.
     */
    private void runKernel(int size) {
        final int dimensions = 1;
        
        //Create pointer buffer for the dimensions.
        PointerBuffer globalWorkSize = BufferUtils.createPointerBuffer(dimensions);
        
        globalWorkSize.put(size);
        
        //Start running the kernel
        CL10.clEnqueueNDRangeKernel(this.session.getQueue(), this.kernel, 
                dimensions, null, globalWorkSize, null, null, this.event);   
    }
    
    /**
     * Waits for any instances of this tool to finish
     */
    public void finish() {
        //Wait for 
        CL10.clWaitForEvents(this.event);
    }
    
    /**
     * Converts a source file into a string
     * @param path the path of the file to load
     * @return the contents of the file as a string.
     */
    private static String loadSource(String path) {

        String resultString = null;
        BufferedReader reader = null;
        try {
            //Get the file
            File clSourceFile = new File(CLTool.class.getClassLoader().getResource(path).toURI());
            //Create a reader for the file
            reader = new BufferedReader(new FileReader(clSourceFile));
            //Read the file
            String line;
            StringBuilder result = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                result.append(line);
                result.append("\n");
            }
            resultString = result.toString();
        } catch (URISyntaxException ex) {
            Logger.getLogger(CLTool.class.getName()).log(Level.SEVERE,
                    "Error converting file name into URI", ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CLTool.class.getName()).log(Level.SEVERE,
                    "Error retrieving OpenCL source file", ex);
        } catch (IOException ex) {
            Logger.getLogger(CLTool.class.getName()).log(Level.SEVERE,
                    "Error reading OpenCL source file", ex);
        } finally {
            if (reader != null) {
                try {
                    //clean up resources
                    reader.close();
                } catch (IOException ex) {
                    Logger.getLogger(CLTool.class.getName()).log(Level.SEVERE,
                            "Error closing OpenCL source file", ex);
                }
            }
        }
        
        //Return result
        return resultString;
    }
}
