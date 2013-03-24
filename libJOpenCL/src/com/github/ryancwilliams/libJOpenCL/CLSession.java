/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.ryancwilliams.libJOpenCL;

import java.nio.IntBuffer;
import java.util.List;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opencl.CL;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CLCommandQueue;
import org.lwjgl.opencl.CLContext;
import org.lwjgl.opencl.CLDevice;
import org.lwjgl.opencl.CLPlatform;
import org.lwjgl.opencl.Util;

/**
 * This class is used to manage a OpenCL session on a device on your system.
 * @author ryancwilliams
 */
public class CLSession {
    
    private final CLPlatform platform;
    private final List<CLDevice> devices;
    private final CLContext contex;
    private final CLCommandQueue queue;
    private IntBuffer errorBuffer;

    /**
     * Opens a CLSession on one of the GPUs installed in the system.
     * @throws LWJGLException If an error exception occurs while creating the session
     */
    public CLSession() throws LWJGLException {
        this(CL10.CL_DEVICE_TYPE_GPU);
    }
    
    /**
     * Opens a CLSession on one of the devices of the provided type
     * @param clDeviceType The Device type to use for the session, use the 
     * device type fields from org.lwjgl.opencl.CL10
     * @throws LWJGLException If an error exception occurs while creating the session
     */
    public CLSession(int clDeviceType) throws LWJGLException {
        this(CLPlatform.getPlatforms().get(0),clDeviceType);
    }
    
    /**
     * Opens a CLSession on one of the devices of the provided type
     * @param platform The Platform to open the session on.
     * @param clDeviceType The Device type to use for the session, use the 
     * device type fields from org.lwjgl.opencl.CL10
     * @throws LWJGLException If an error exception occurs while creating the session
     */
    public CLSession(CLPlatform platform, int clDeviceType) throws LWJGLException {
        this(platform,platform.getDevices(clDeviceType));
    }
    
    /**
     * Opens a CLSession on one of the devices provided
     * @param platform The Platform to open the session on.
     * @param devices The Devices to use for the session.
     * @throws LWJGLException If an error exception occurs while creating the session 
     */
    public CLSession(CLPlatform platform, List<CLDevice> devices) throws LWJGLException {
        //Load Varables
        this.platform = platform;
        this.devices = devices;
        
        //Create Error Buffer
        this.errorBuffer = BufferUtils.createIntBuffer(1);
        
        //Start OpenCL
        CL.create();
        
        //Create CL-Contex
        this.contex = CLContext.create(this.platform, this.devices, this.errorBuffer);
        
        //Check for errors
        Util.checkCLError(this.errorBuffer.get(0));
        
        //Create command Queue
        this.queue = CL10.clCreateCommandQueue(this.contex, this.devices.get(0),
                CL10.CL_QUEUE_PROFILING_ENABLE, this.errorBuffer);
        
        //Check for errors
        Util.checkCLError(this.errorBuffer.get(0));
    }
    
    /**
     * Closes the current CLSession.
     */
    public void CloseSession() {
        
        //Cleanup refferences to OpenCL
        CL10.clReleaseCommandQueue(this.queue);
        CL10.clReleaseContext(this.contex);
        
        //Stop OpenCL
        CL.destroy();
    }

    @Override
    protected void finalize() throws Throwable {
        this.CloseSession();
        super.finalize();
    }
}
