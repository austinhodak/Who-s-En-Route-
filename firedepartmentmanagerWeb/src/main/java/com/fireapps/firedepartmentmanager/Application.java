package com.fireapps.firedepartmentmanager;

/**
 * Created by austinhodak on 7/28/15.
 */
public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //Optimizely.startOptimizelyWithAPIToken(getString(R.string.com_optimizely_api_key), this);
        /*SmsRadar.initializeSmsRadarService(getApplicationContext(), new SmsListener() {
            @Override
            public void onSmsSent(Sms sms) {

            }

            @Override
            public void onSmsReceived(Sms sms) {
                Log.d("FDM", "TEXT RECEIVED");

                try {

                    if (sms != null) {


                            //SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);

                            String phoneNumber = sms.getAddress();

                            String senderNum = phoneNumber;

                            String message = sms.getMsg();

                            Log.i("SmsReceiver", "senderNum: " + senderNum + "; message: " + message);


                            // Show alert
                    *//*int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(context, "senderNum: "+ senderNum + ", message: " + message, duration);
                    toast.show();*//*

//senderNum.equalsIgnoreCase("wc911text@warren-county.net") &&

                            if (!message.startsWith("(Msg")) {

                                Pattern p = Pattern.compile("(\\bLoc:\\b)(.*?)(\\bXSt\\b)");
                                Matcher m = p.matcher(message);
                                StringBuilder sb = new StringBuilder();
                                while (m.find()) {
                                    sb.append(m.group(2));
                                }

                                String sbb = sb.toString();
                                String[] arr = sbb.split(" ");

                                StringBuilder builder = new StringBuilder();

                                for (String s : arr) {
                                    builder.append(s).append("+");
                                }

                                String address = method(builder.toString());

                                //Toast.makeText(context, address, Toast.LENGTH_SHORT).show();
                                Log.i("FDM", "Address: " + address);
                                //showMap(Uri.parse("geo:41.8356137,-79.381438?q=" + address));


                                //Check Call Type
                                if (message.startsWith("(FIRE")) {
                                    //Fire - Priority 1 Unless Standby

                                    if (message.startsWith("(FIRE-STRUCTURE)")) {
                                        //P1
                                        updateCurrentCallInfo(address, "Structure Fire", true, message);

                                    } else if (message.startsWith("(FIRE-ALARM)")) {
                                        //P1
                                        updateCurrentCallInfo(address, "Fire Alarm", true, message);

                                    } else if (message.startsWith("(FIRE-ASSIST)")) {
                                        //P2 Standby
                                        updateCurrentCallInfo(address, "Fire Assist", false, message);

                                    } else if (message.startsWith("(FIRE-BRUSH)")) {
                                        //P1
                                        updateCurrentCallInfo(address, "Brush Fire", true, message);

                                    } else if (message.startsWith("(FIRE-VEHICLE)")) {
                                        //P1
                                        updateCurrentCallInfo(address, "Vehicle Fire", true, message);

                                    } else {
                                        //Unknown Fire - Use Discretion
                                        updateCurrentCallInfo(address, "Unknown Fire", false, message);
                                    }

                                } else if (message.startsWith("(MVA")) {
                                    //MVA - Priority 1
                                    updateCurrentCallInfo(address, "MVA", true, message);

                                } else if (message.startsWith(("(TREES"))) {
                                    //Trees Down - Use Discretion
                                    updateCurrentCallInfo(address, "Trees/Wires Down", false, message);

                                } else {
                                    //Probably EMS - Check for Priority
                                    if (message.contains("PRI 1")) {
                                        //Priority 1 According to Text Contents
                                        updateCurrentCallInfo(address, "EMS", true, message);
                                    } else {
                                        //P2
                                        updateCurrentCallInfo(address, "EMS", false, message);
                                    }

                                }
                            } else if (message.startsWith("(Msg")) {
                                addMessage(message);
                            }
                        } // end for loop
                     // bundle is null

                } catch (Exception e) {
                    Log.e("SmsReceiver", "Exception smsReceiver" + e);

                }
            }
        });*/
    }

    public String method(String str) {
        if (str.length() > 0 && str.charAt(str.length() - 1) == '+') {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    /*public void updateCurrentCallInfo(final String address, final String callType, final boolean priorityCall, final String message) {
        ParseQuery<IncidentObject> query = ParseQuery.getQuery(IncidentObject.class);
        query.whereEqualTo("department", ParseUser.getCurrentUser().getParseObject("departmentP"));
        query.getFirstInBackground(new GetCallback<IncidentObject>() {
            @Override
            public void done(IncidentObject object, ParseException e) {
                if (object.getBoolean("isActive")) {
                    //Incident Already Active, Check Address and Call Type to see if text is another incident.
                    if (!object.getString("location").contains(address)) {
                        //Different Address = Separate Incident. Make New.
                        IncidentObject incidentObject = new IncidentObject();
                        incidentObject.add("location", address);
                        incidentObject.add("callType", callType);
                        incidentObject.add("isPriority", priorityCall);
                        incidentObject.add("initialText", message);
                        incidentObject.add("department", ParseUser.getCurrentUser().getParseObject("departmentP"));
                        incidentObject.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {

                            }
                        });
                    } else if (!object.getString("callType").contains(callType)){
                        //Address Same, Call Type Different. Make New
                        IncidentObject incidentObject = new IncidentObject();
                        incidentObject.add("location", address);
                        incidentObject.add("callType", callType);
                        incidentObject.add("isPriority", priorityCall);
                        incidentObject.add("initialText", message);
                        incidentObject.add("department", ParseUser.getCurrentUser().getParseObject("departmentP"));
                        incidentObject.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {

                            }
                        });
                    }
                } else if (object.getString("initialText").equals(message)){
                    //Incident NOT Active. Text Received After Call Possibly. Do NOTHING...
                } else {
                    //NEW INCIDENT. CREATE OBJECT AND NOTIFY OTHERS WHO HAVEN'T RECEIVED TEXT.
                    IncidentObject incidentObject = new IncidentObject();
                    incidentObject.add("location", address);
                    incidentObject.add("callType", callType);
                    incidentObject.add("isPriority", priorityCall);
                    incidentObject.add("initialText", message);
                    incidentObject.add("department", ParseUser.getCurrentUser().getParseObject("departmentP"));
                    incidentObject.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {

                        }
                    });
                }
            }
        });
    }

    public void addMessage(final String message) {
        ParseQuery<IncidentObject> query = ParseQuery.getQuery(IncidentObject.class);
        query.whereEqualTo("department", ParseUser.getCurrentUser().getParseObject("departmentP"));
        query.getFirstInBackground(new GetCallback<IncidentObject>() {
            @Override
            public void done(IncidentObject object, ParseException e) {
                if (object.getBoolean("isActive")) {
                    JSONArray jsonArray = object.getMessages();
                    jsonArray.put(message);
                    object.setMessages(jsonArray);
                    object.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {

                        }
                    });
                }
            }
        });
    }*/
}
