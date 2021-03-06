package org.bbop.apollo.gwt.client.rest;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import org.bbop.apollo.gwt.client.Annotator;
import org.bbop.apollo.gwt.client.AnnotatorPanel;
import org.bbop.apollo.gwt.client.dto.GroupInfo;
import org.bbop.apollo.gwt.client.dto.GroupOrganismPermissionInfo;
import org.bbop.apollo.gwt.client.dto.UserInfo;
import org.bbop.apollo.gwt.client.event.GroupChangeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by ndunn on 3/30/15.
 */
public class GroupRestService {


    public static void loadGroups(final List<GroupInfo> groupInfoList) {

        RequestCallback requestCallback = new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                groupInfoList.clear();
                JSONValue returnValue = JSONParser.parseStrict(response.getText());
                JSONArray array = returnValue.isArray();

                for (int i = 0; i < array.size(); i++) {
                    JSONObject object = array.get(i).isObject();

                    GroupInfo groupInfo = new GroupInfo();
                    groupInfo.setId((long) object.get("id").isNumber().doubleValue());
                    groupInfo.setName(object.get("name").isString().stringValue());
                    groupInfo.setNumberOfUsers((int) object.get("numberOfUsers").isNumber().doubleValue());

                    List<UserInfo> userInfoList = new ArrayList<>();


                    if(object.get("users")!=null){
                        JSONArray usersArray = object.get("users").isArray() ;
                        for(int j =0 ; j < usersArray.size() ; j++){
                            JSONObject userObject = usersArray.get(j).isObject();
                            UserInfo userInfo = new UserInfo(userObject);
                            userInfoList.add(userInfo);
                        }
                    }


                    groupInfo.setUserInfoList(userInfoList);



                    // TODO: use shared permission enums
                    JSONArray organismArray = object.get("organismPermissions").isArray();
                    Map<String, GroupOrganismPermissionInfo> organismPermissionMap = new TreeMap<>();
                    for (int j = 0; j < organismArray.size(); j++) {
                        JSONObject organismPermissionJsonObject = organismArray.get(j).isObject();
                        GroupOrganismPermissionInfo groupOrganismPermissionInfo = new GroupOrganismPermissionInfo();
                        if(organismPermissionJsonObject.get("id")!=null){
                            groupOrganismPermissionInfo.setId((long) organismPermissionJsonObject.get("id").isNumber().doubleValue());
                        }
                        groupOrganismPermissionInfo.setGroupId((long) organismPermissionJsonObject.get("groupId").isNumber().doubleValue());
                        groupOrganismPermissionInfo.setOrganismName(organismPermissionJsonObject.get("organism").isString().stringValue());
                        if(organismPermissionJsonObject.get("permissions")!=null) {
                            JSONArray permissionsArray = JSONParser.parseStrict(organismPermissionJsonObject.get("permissions").isString().stringValue()).isArray();
                            for (int permissionIndex = 0; permissionIndex < permissionsArray.size(); ++permissionIndex) {
                                String permission = permissionsArray.get(permissionIndex).isString().stringValue();
                                switch (permission) {
                                    case "ADMINISTRATE":
                                        groupOrganismPermissionInfo.setAdmin(true);
                                        break;
                                    case "WRITE":
                                        groupOrganismPermissionInfo.setWrite(true);
                                        break;
                                    case "EXPORT":
                                        groupOrganismPermissionInfo.setExport(true);
                                        break;
                                    case "READ":
                                        groupOrganismPermissionInfo.setRead(true);
                                        break;

                                    default:
                                        Window.alert("not sure what to do wtih this: " + permission);
                                }
                            }
                        }


                        organismPermissionMap.put(groupOrganismPermissionInfo.getOrganismName(), groupOrganismPermissionInfo);
                    }
                    groupInfo.setOrganismPermissionMap(organismPermissionMap);



                    groupInfoList.add(groupInfo);
                }
            }

            @Override
            public void onError(Request request, Throwable exception) {
                Window.alert("errror retrieving groups");
            }
        };

        RestService.sendRequest(requestCallback, "group/loadGroups/");
    }

    public static void updateGroup(final GroupInfo selectedGroupInfo) {
        RequestCallback requestCallback = new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                Annotator.eventBus.fireEvent(new GroupChangeEvent(GroupChangeEvent.Action.RELOAD_GROUPS));
            }

            @Override
            public void onError(Request request, Throwable exception) {
                Window.alert("error updating group "+selectedGroupInfo.getName()+" "+exception);
            }
        };
        RestService.sendRequest(requestCallback, "group/updateGroup/", "data="+selectedGroupInfo.toJSON().toString());
    }

    public static void deleteGroup(final GroupInfo selectedGroupInfo) {
        RequestCallback requestCallback = new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                Annotator.eventBus.fireEvent(new GroupChangeEvent(GroupChangeEvent.Action.RELOAD_GROUPS));
            }

            @Override
            public void onError(Request request, Throwable exception) {
                Window.alert("error updating group "+selectedGroupInfo.getName()+" "+exception);
            }
        };
        RestService.sendRequest(requestCallback, "group/deleteGroup/", "data="+selectedGroupInfo.toJSON().toString());
    }

    public static void addNewGroup(final GroupInfo selectedGroupInfo) {
        RequestCallback requestCallback = new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                Annotator.eventBus.fireEvent(new GroupChangeEvent(GroupChangeEvent.Action.ADD_GROUP));
            }

            @Override
            public void onError(Request request, Throwable exception) {
                Window.alert("error updating group "+selectedGroupInfo.getName()+" "+exception);
            }
        };
        RestService.sendRequest(requestCallback, "group/createGroup/", "data="+selectedGroupInfo.toJSON().toString());
    }

    public static void updateOrganismPermission(GroupOrganismPermissionInfo object) {
        RequestCallback requestCallback = new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                GWT.log("success");
//                loadUsers(userInfoList);
            }

            @Override
            public void onError(Request request, Throwable exception) {
                Window.alert("Error updating permissions: " + exception);
            }
        };
        RestService.sendRequest(requestCallback, "group/updateOrganismPermission", "data=" + object.toJSON());
    }
}
