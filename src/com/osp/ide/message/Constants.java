package com.osp.ide.message;

public class Constants {
	public static final String	BADA_DEFAULT_CHARSET	= "UTF8";
	public static final int OSP_MSG_TYPE_PRIMARY 		= 0;
	public static final int OSP_MSG_TYPE_SECONDARY 		= 1;
	
	public static final int OSP_MSG_IN_HOST_DEBUG 		= 0;
	public static final int OSP_MSG_IN_HOST_TRACE 		= 1;
	public static final int OSP_MSG_IN_TARGET_DEBUG 	= 2;
	public static final int OSP_MSG_IN_TARGET_TRACE 	= 3;
	public static final int OSP_MSG_IN_OUT_HAT 			= 4;
	
	public static final int BADA_MSG_IN_BROKER					= 6;
	public static final int BADA_MSG_IN_BROKER_TRANSFER			= 1;
	public static final int BADA_MSG_IN_BROKER_DEBUGGING		= 2;
	public static final int BADA_MSG_IN_BROKER_INFORMATION		= 9;
	
	public static final int BADA_MSG_TRANS_START_APPLICATION	= 1;
	public static final int BADA_MSG_TRANS_START_FILE 			= 2;
	public static final int BADA_MSG_TRANS_FILE					= 3;
	public static final int BADA_MSG_TRANS_STOP_FILE			= 4;
	public static final int BADA_MSG_TRANS_STOP_APPLICATION		= 5;
	public static final int BADA_MSG_BROKER_ERROR				= 11;
	
	public final static String FILTER_FILE_NAME 		= "Filter.osp";
	public final static String REPLACE_FILE_NAME 		= "Replace.osp";
	
	public static final String OSP_RM_FILTER_PROCESS	= "PROCESSMGR";
//	public static final String OSP_RM_FILTER_THREAD		= "OSAL";
	
	public static final String BADA_RM_FILTER_HEAP		= "MEMMGR";
	public static final String BADA_RM_HEAP_REQUEST_PRE	= "0|2|MID_MEMMGR,0xFF";
	public static final String BADA_RM_PROCESS_REQUEST	= "0|2|MID_PROCESSMGR,0xFF";
	public static final String BADA_RM_HEAP_REQUEST		= "1500|1501|";
	public static final int	   BADA_MEMORY_REQUEST_DELAY = 1000;

	public static final String BADA_PHONE_STATUS_OFF	= "PHONESTATUS:OFF";
	public static final String BADA_PHONE_STATUS_00		= "PHONESTATUS:00";
	public static final String BADA_PHONE_STATUS_04		= "PHONESTATUS:04";
	public static final String BADA_PHONE_STATUS_GDB	= "PHONESTATUS:ENABLE_GDB";
	
	public static final String BADA_PHONE_SHUTDOWN		= "BADA_PHONE_SHUTDOWN";
	public static final String BADA_EXIT_SIMULATOR		= "32767";
	
//	public static final int OSP_MGS_CREATE_PROCESS		= 0;
//	public static final int OSP_MGS_TERMINATE_PROCESS	= 1;

//	public static final int OSP_MGS_CREATE_THREAD		= 0;
//	public static final int OSP_MGS_DELETE_THREAD		= 1;

//	public static final int OSP_MGS_HEAP_RESPONSE		= 4;
//	public static final int OSP_MGS_MEMORY_LEAK			= 5;
	
//	public static final String OSP_RESOURCE_MEMORYLEAK		= "Memory Leak";
//	public static final String OSP_RESOURCE_MEMORYSTATUS	= "Memory Status";
	
	public static final String OSP_RESOURCE_MEMORY			= "Memory";
	public static final String OSP_RESOURCE_THREAD			= "Thread";
	public static final String OSP_RESOURCE_TIMER			= "Timer";
	public static final String OSP_RESOURCE_FILE			= "File";
	public static final String OSP_RESOURCE_DATABASE		= "Database";
	public static final String OSP_RESOURCE_REGISTERY		= "Registery";
	public static final String OSP_RESOURCE_SOCKET			= "Socket";
	public static final String OSP_RESOURCE_FORM			= "Form";
	
	public static final String BADA_RM_MESSAGES_TYPE[] 		= {"Memory", "Thread", "Timer", "File", "Database", "Registery", "Socket", "Form"};
	
//	public static final int OSP_ID_MEMORYSTATUS		= 101;
//	public static final int OSP_ID_MEMORYLEAK		= 102;
	
	public static final int OSP_ID_MEMORY			= 10000;
	public static final int OSP_ID_THREAD			= 10100;
	public static final int OSP_ID_TIMER			= 10400;
	public static final int OSP_ID_FILE				= 10500;
	public static final int OSP_ID_DATABASE			= 10700;
	public static final int OSP_ID_REGISTERY		= 10800;
	public static final int OSP_ID_SOCKET			= 10900;
	public static final int OSP_ID_FORM				= 11000;
	public static final int OSP_ID_APPLICATION		= 11100;
	
	public static final String FILTER_INFO			= "INFO";
	public static final String FILTER_DEBUG			= "DEBUG";
	public static final String FILTER_EXCEPTION		= "EXCEPTION";
	
	// Messages not to be inserted into Output View
	public static final String EXCEPT_FILETER_UNITTEST_MESSAGE_01	= "UnitTest->";
}