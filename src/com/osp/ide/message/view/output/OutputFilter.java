package com.osp.ide.message.view.output;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.osgi.service.datalocation.Location;

import com.osp.ide.message.Constants;
import com.osp.ide.message.OSPMessage;
import com.osp.ide.message.socket.NetManager;

public class OutputFilter extends ViewerFilter {
	
	private List<String>					filterList;
	private List<String>                    filterTypes;
	private List<String>					exceptFilterTypes;
	private static HashMap<String, String>	replaceList;
	private boolean	bCaseSensitive;

	public OutputFilter() {
		LoadFilter();
	}
	
	public static void setReplaceList(HashMap<String, String> hm) {
		replaceList = hm;
	}
	
	public void LoadFilter() {
		if(filterList != null)
			filterList.clear();
		else
			filterList = new ArrayList<String>();
		
		if(filterTypes != null)
			filterTypes.clear();
		else
			filterTypes = new ArrayList<String>();
		
		if(exceptFilterTypes != null)
			exceptFilterTypes.clear();
		else
			exceptFilterTypes = new ArrayList<String>();
		
		exceptFilterTypes.add(Constants.EXCEPT_FILETER_UNITTEST_MESSAGE_01);
		
		NetManager netManager = NetManager.getInstance();
		if(netManager.isFilter_info())
			filterTypes.add(Constants.FILTER_INFO);
		
		if(netManager.isFilter_debug())
			filterTypes.add(Constants.FILTER_DEBUG);
		
		if(netManager.isFilter_exception())
			filterTypes.add(Constants.FILTER_EXCEPTION);
		
		Location location = Platform.getConfigurationLocation();
		if (location == null || location.isReadOnly()) {
			return;
		}
		
		BufferedReader reader = null;
		try {
			String filename = location.getURL().getPath() + Constants.FILTER_FILE_NAME;
			File file = new File(filename);
			if(!file.exists())	return;
			
//			reader = new BufferedReader(new FileReader(filename));
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), Charset.defaultCharset()));
			String line = reader.readLine();
			if(line != null) {
				String[] l = line.split("=");
				if(l[0].equals("Case sensitive")) {
					if(l[1].equals("true"))
						bCaseSensitive = true;
					else
						bCaseSensitive = false;
				}
			}
			while((line = reader.readLine()) != null) {
				if(line.length()> 0) {
					filterList.add(line);
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		finally {
			try { if(reader != null) reader.close(); }
			catch (IOException e) { e.printStackTrace(); }
		}
	}
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		OSPMessage osp = (OSPMessage) element;
		
		//if(osp.getDest() == Constants.OSP_MSG_IN_HOST_DEBUG ||
		//		osp.getDest() == Constants.OSP_MSG_IN_TARGET_DEBUG) {
			
			if(!FilteringTextMessage(osp.getOriginalMessage()))
				return false;

			osp.setOutputMessage(GetReplaceString(osp.getOriginalMessage()));
			return true;
		//}
		
		//return false;
	}

	private boolean FilteringTextMessage(String message) {
		boolean bRet = false;
		
		if(!bCaseSensitive)
			message = message.toLowerCase(Locale.getDefault());
		
		if (filterTypes.size() == 0)
            return bRet;
		
		if (exceptFilterTypes.size() > 0) {
			for (String exceptFilterType : exceptFilterTypes) {
				if (!bCaseSensitive)
					exceptFilterType = exceptFilterType.toLowerCase(Locale.getDefault());

				if (message.indexOf(exceptFilterType) != -1)
					return bRet;
					
			}
		}
		
		if (filterTypes.size() > 0) {
            for (String filterType : filterTypes) {
                if (!bCaseSensitive)
                    filterType = filterType.toLowerCase(Locale.getDefault());
                
                if (message.indexOf(filterType) != -1) {
                    if (filterList.size() > 0) {
                        for (String filter : filterList) {
                            if (!bCaseSensitive)
                                filter = filter.toLowerCase(Locale.getDefault());

                            if (message.indexOf(filter) != -1) {
                                return true;
                            }
                        }
                    } else {
                        bRet = true;
                        break;
                    }
                }
            }
        }
		else
			bRet = true;
		
		return bRet;
	}
	
	private String GetReplaceString(String message) {
		String result = message;
		if(replaceList != null) {
			for(Iterator iter = replaceList.entrySet().iterator(); iter.hasNext();) {
				Entry entry = (Entry)iter.next();
				int index = message.indexOf((String)entry.getKey());
				if(index >= 0) {
					message = message.replaceAll((String)entry.getKey(), (String)entry.getValue());
				}
			}
			result = message;
		}
		
		return result;
	}
	
}
