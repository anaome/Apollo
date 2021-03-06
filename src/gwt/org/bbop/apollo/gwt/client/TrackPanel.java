package org.bbop.apollo.gwt.client;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.http.client.*;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.json.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import org.bbop.apollo.gwt.client.dto.TrackInfo;
import org.bbop.apollo.gwt.client.resources.TableResources;
import org.gwtbootstrap3.client.ui.TextBox;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.view.client.CellPreviewEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

//import com.google.gwt.user.client.ui.ListBox;
//import org.gwtbootstrap3.client.ui.gwt.DataGrid;

/**
 * Created by ndunn on 12/16/14.
 */
public class TrackPanel extends Composite {
    interface TrackUiBinder extends UiBinder<Widget, TrackPanel> {
    }

    private static TrackUiBinder ourUiBinder = GWT.create(TrackUiBinder.class);

    @UiField
    static TextBox nameSearchBox;
    @UiField
    HTML trackName;
    @UiField
    HTML trackType;
    @UiField
    HTML trackCount;
    @UiField
    HTML trackDensity;

    private DataGrid.Resources tablecss = GWT.create(TableResources.TableCss.class);
    @UiField(provided = true)
    DataGrid<TrackInfo> dataGrid = new DataGrid<TrackInfo>(100, tablecss);
    @UiField
    SplitLayoutPanel layoutPanel;
    @UiField
    Tree optionTree;



    public static ListDataProvider<TrackInfo> dataProvider = new ListDataProvider<>();
    private static List<TrackInfo> trackInfoList = new ArrayList<>();
    private static List<TrackInfo> filteredTrackInfoList = dataProvider.getList();
    private SingleSelectionModel<TrackInfo> singleSelectionModel = new SingleSelectionModel<TrackInfo>();
    private boolean trackSelectionFix; // this fixes the fact that firefox requires two clicks to select a CheckboxCell

    public TrackPanel() {
        exportStaticMethod();
        trackSelectionFix=true;

        Widget rootElement = ourUiBinder.createAndBindUi(this);
        initWidget(rootElement);

        dataGrid.setWidth("100%");

        // Set the message to display when the table is empty.
        // fix selected style: http://comments.gmane.org/gmane.org.google.gwt/70747
        dataGrid.setEmptyTableWidget(new Label("No tracks!"));

        Column<TrackInfo, Boolean> showColumn = new Column<TrackInfo, Boolean>(new CheckboxCell(true, false)) {
            @Override
            public Boolean getValue(TrackInfo track) {
                return track.getVisible();
            }
        };
        dataGrid.addCellPreviewHandler(new CellPreviewEvent.Handler<TrackInfo>() {

            @Override
            public void onCellPreview(final CellPreviewEvent<TrackInfo> event) {

                if (BrowserEvents.CLICK.equals(event.getNativeEvent().getType())) {
                    trackSelectionFix=false;
                    final TrackInfo value = event.getValue();
                    final Boolean state = !event.getDisplay().getSelectionModel().isSelected(value);
                    event.getDisplay().getSelectionModel().setSelected(value, state);
                    event.setCanceled(true);
                }
            }
        });

        showColumn.setFieldUpdater(new FieldUpdater<TrackInfo, Boolean>() {
            /**
             * TODO: emulate . . underTrackList . . Create an external function in Annotrack to then call from here
             * a good example: http://www.springindepth.com/book/gwt-comet-gwt-dojo-cometd-spring-bayeux-jetty.html
             * uses DOJO publish mechanism (http://dojotoolkit.org/reference-guide/1.7/dojo/publish.html)

             * @param index
             * @param trackInfo
             * @param value
             */
            @Override
            public void update(int index, TrackInfo trackInfo, Boolean value) {
                JSONObject jsonObject = trackInfo.getPayload();
                trackInfo.setVisible(value);
                if (value) {
                    jsonObject.put("command", new JSONString("show"));
                } else {
                    jsonObject.put("command", new JSONString("hide"));
                }

                MainPanel.executeFunction("handleTrackVisibility", jsonObject.getJavaScriptObject());
            }
        });
        showColumn.setSortable(true);

        TextColumn<TrackInfo> nameColumn = new TextColumn<TrackInfo>() {
            @Override
            public String getValue(TrackInfo track) {
                return track.getName();
            }
        };
        nameColumn.setSortable(true);



        dataGrid.addColumn(showColumn, "Show");
        dataGrid.addColumn(nameColumn, "Name");
        dataGrid.setColumnWidth(0, "10%");
        dataGrid.setSelectionModel(singleSelectionModel);
        singleSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if(!trackSelectionFix) setTrackInfo(singleSelectionModel.getSelectedObject());
                trackSelectionFix=true;
            }
        });


        dataProvider.addDataDisplay(dataGrid);

        Scheduler.get().scheduleDeferred(new Command() {
            public void execute() {
                reload();
            }
        });

        ColumnSortEvent.ListHandler<TrackInfo> sortHandler = new ColumnSortEvent.ListHandler<TrackInfo>(filteredTrackInfoList);
        dataGrid.addColumnSortHandler(sortHandler);

        sortHandler.setComparator(showColumn, new Comparator<TrackInfo>() {
            @Override
            public int compare(TrackInfo o1, TrackInfo o2) {
                if (o1.getVisible() == o2.getVisible()) {
                    return 0;
                }  else if (o1.getVisible()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        sortHandler.setComparator(nameColumn, new Comparator<TrackInfo>() {
            @Override
            public int compare(TrackInfo o1, TrackInfo o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

    }

    private void setTrackInfo(TrackInfo selectedObject) {
        if (selectedObject == null) {
            trackName.setText("");
            trackType.setText("");
            optionTree.clear();
        } else {
            trackName.setText(selectedObject.getName());
            trackType.setText(selectedObject.getType());
            optionTree.clear();
            JSONObject jsonObject = selectedObject.getPayload();
            setOptionDetails(jsonObject);
        }
    }

    private void setOptionDetails(JSONObject jsonObject) {
        for (String key : jsonObject.keySet()) {
            TreeItem treeItem = new TreeItem();
            treeItem.setHTML(generateHtmlFromObject(jsonObject, key));
            if (jsonObject.get(key).isObject() != null) {
                treeItem.addItem(generateTreeItem(jsonObject.get(key).isObject()));
            }
            optionTree.addItem(treeItem);
        }
    }

    private String generateHtmlFromObject(JSONObject jsonObject, String key) {
        if (jsonObject.get(key) == null) {
            return key;
        } else if (jsonObject.get(key).isObject() != null) {
            return key;
        } else {
            return "<b>" + key + "</b>: " + jsonObject.get(key).toString().replace("\\", "");
        }
    }

    private TreeItem generateTreeItem(JSONObject jsonObject) {
        TreeItem treeItem = new TreeItem();

        for (String key : jsonObject.keySet()) {
            treeItem.setHTML(generateHtmlFromObject(jsonObject, key));
            if (jsonObject.get(key).isObject() != null) {
                treeItem.addItem(generateTreeItem(jsonObject.get(key).isObject()));
            }
        }


        return treeItem;
    }


    @UiHandler("nameSearchBox")
    public void doSearch(KeyUpEvent keyUpEvent) {

        filterList();
    }

    static void filterList() {
        String text = nameSearchBox.getText();
        filteredTrackInfoList.clear();
        for (TrackInfo trackInfo : trackInfoList) {
            if (trackInfo.getName().toLowerCase().contains(text.toLowerCase()) &&
                    !isReferenceSequence(trackInfo) &&
                    !isAnnotationTrack(trackInfo)) {
                filteredTrackInfoList.add(trackInfo);
            }
        }
    }

    private static boolean isAnnotationTrack(TrackInfo trackInfo) {
        return trackInfo.getName().equalsIgnoreCase("User-created Annotations");
    }

    private static boolean isReferenceSequence(TrackInfo trackInfo) {
        return trackInfo.getName().equalsIgnoreCase("Reference sequence");
    }

    public static native void exportStaticMethod() /*-{
        $wnd.loadTracks = $entry(@org.bbop.apollo.gwt.client.TrackPanel::updateTracks(Ljava/lang/String;));
    }-*/;

    public void reload() {
        JSONObject commandObject = new JSONObject();
        commandObject.put("command", new JSONString("list"));
        MainPanel.executeFunction("handleTrackVisibility", commandObject.getJavaScriptObject());
    }

    public static void updateTracks(String jsonString) {
        JSONArray returnValueObject = JSONParser.parseStrict(jsonString).isArray();
        updateTracks(returnValueObject);
    }

    public List<String> getTrackList(){
        if(trackInfoList.size()==0){
            reload() ;
        }
        List<String> trackListArray = new ArrayList<>();
        for(TrackInfo trackInfo : trackInfoList){
            if(trackInfo.getVisible()){
                trackListArray.add(trackInfo.getName());
            }
        }
        return trackListArray;
    }

    public static void updateTracks(JSONArray array) {
        trackInfoList.clear();

        for (int i = 0; i < array.size(); i++) {
            JSONObject object = array.get(i).isObject();
            TrackInfo trackInfo = new TrackInfo();
            trackInfo.setName(object.get("key").isString().stringValue());
            trackInfo.setLabel(object.get("label").isString().stringValue());
            trackInfo.setType(object.get("type").isString().stringValue());
            if (object.get("visible") != null) {
                trackInfo.setVisible(object.get("visible").isBoolean().booleanValue());
            } else {
                trackInfo.setVisible(false);
            }
            // todo, don't need all of this really, for now . .
            trackInfo.setPayload(object);
            if (object.get("urlTemplate") != null) {
                trackInfo.setUrlTemplate(object.get("urlTemplate").isString().stringValue());
            }
            trackInfoList.add(trackInfo);
        }
        filterList();
    }

}
