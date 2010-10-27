package com.osp.ide.message;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.osp.ide.message.socket.NetManager;

/**
 * The activator class controls the plug-in life cycle
 */
public class MessagePlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.osp.ide.message";

	// The shared instance
	private static MessagePlugin plugin;
	
	/**
	 * The constructor
	 */
	public MessagePlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		//NetManager.getInstance().startServer();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		NetManager.getInstance().stopServer();
		NetManager.getInstance().dispose();
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static MessagePlugin getDefault() {
		return plugin;
	}

}
