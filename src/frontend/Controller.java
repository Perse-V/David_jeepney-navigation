package frontend;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.event.ActionEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.scene.image.ImageView;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import netscape.javascript.JSObject;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import algorithm.Node;
import algorithm.Edge;
import algorithm.AStar;

// Controller class for handling the UI and logic of the application
public class Controller implements Initializable {

    // Maps to store node data and edges
    private Map<String, Node> nodeMap;
    private List<Edge> edges;
    
    // Selected source and destination locations
    private String srcLocation;
    private String destLocation;

    // JavaFX components injected via FXML
    private Stage stage;
    private Scene scene;
    private Parent root;
    
    @FXML
    private WebView webView;
    private WebEngine webEngine;
    
    @FXML
    private ComboBox<Location> currLocation;
    
    @FXML
    private ComboBox<Location> trgtLocation;
    
    @FXML
    private VBox vboxContent;
    
    @FXML
    private ScrollPane scrollPane;
    
    @FXML
    private Label locationAddress;
    
    @FXML
    private Label locationName;
    
    @FXML
    private ImageView stationImage;
    
    @FXML
    private ImageView stationLogo;
    
    @FXML
    private ImageView addressLogo;
    
    private boolean isImageSet;
     
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize current and target location ComboBoxes
        if (currLocation != null && trgtLocation != null) {
            currLocation.setOnAction(event -> getCurrentLocation(event));
            trgtLocation.setOnAction(event -> getTargetLocation(event));

            // Define the stations with their corresponding city
            List<Location> locations = List.of(
                new Location("Phase 1 Church", "North Caloocan"),
                new Location("Monumento", "South Caloocan"),
                new Location("VGC Terminal", "Valenzuela"),
                new Location("Polo Market", "Valenzuela"),
                new Location("Novaliches", "Quezon City"),
                new Location("Gateway Mall", "Quezon City"),
                new Location("SM Fairview", "Quezon City"),
                new Location("Naval St. Navotas", "Navotas"),
                new Location("Fisher Malabon", "Malabon"),
                new Location("Quiapo", "Manila"),
                new Location("San Juan Comelec", "San Juan"),
                new Location("Pasig Blvd. Ext.", "Pasig"),
                new Location("SM Marikina", "Marikina"),
                new Location("EDSA Terminal", "Mandaluyong")
            );

            currLocation.setItems(FXCollections.observableArrayList(locations));
            trgtLocation.setItems(FXCollections.observableArrayList(locations));

            // Set custom cell factory to display location names with guides
            Callback<ListView<Location>, ListCell<Location>> cellFactory = (ListView<Location> param) -> new ListCell<>() {
                @Override
                protected void updateItem(Location item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getStation());
                        
                        Label lblCity = new Label(" " + item.getCity());
                        lblCity.getStyleClass().add("city");
                        setGraphic(lblCity);
                    }
                }
            };
            currLocation.setCellFactory(cellFactory);
            currLocation.setButtonCell(cellFactory.call(null));
            trgtLocation.setCellFactory(cellFactory);
            trgtLocation.setButtonCell(cellFactory.call(null));
        }

        // Initialize WebView for displaying map
        if (webView != null) {
            webEngine = webView.getEngine();

            // Load local HTML file for map display
            String localUrl = Paths.get("src/backend/map.html").toUri().toString();
            webEngine.load(localUrl);
                
            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    System.out.println("Loaded: " + webEngine.getLocation());
                    JSObject window = (JSObject) webEngine.executeScript("window");
                    window.setMember("javaApp", this);
                } else if (newState == Worker.State.FAILED) {
                    System.out.println("Failed to load: " + webEngine.getLocation());
                    webEngine.getLoadWorker().getException().printStackTrace();
                }
            });
        } else {
            System.out.println("webView is null in initialize");
        }
        
        if (scrollPane != null) {
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        }
        
        this.isImageSet = false;
    }
    
    // Getter for the WebView component
    public WebView getWebView() {
        return webView;
    }
    
    // Getter for the WebEngine component
    public WebEngine getWebEngine() {
        return webEngine;
    }
    
    /**
     * Sets the station image based on the given station name.
     * 
     * @param stationName The name of the station.
     */
    public void setStationImage(String stationName) {
        switch (stationName) {
            case "Phase 1 Church":
                stationImage.setImage(new Image("file:src/places/phase-1.jpg")); 
                break;
            case "Novaliches": 
                stationImage.setImage(new Image("file:src/places/novaliches.jpg")); 
                break;
            case "VGC Terminal": 
                stationImage.setImage(new Image("file:src/places/vgc.jpg")); 
                break;
            case "Monumento": 
                stationImage.setImage(new Image("file:src/places/monumento.jpg")); 
                break;
            case "Polo Market": 
                stationImage.setImage(new Image("file:src/places/polo.jpg")); 
                break;
            case "Naval St. Navotas": 
                stationImage.setImage(new Image("file:src/places/navotas.jpg")); 
                break;
            case "Fisher Malabon": 
                stationImage.setImage(new Image("file:src/places/malabon.jpg")); 
                break;
            case "Quiapo": 
                stationImage.setImage(new Image("file:src/places/quiapo.jpg")); 
                break;
            case "San Juan Comelec": 
                stationImage.setImage(new Image("file:src/places/san-juan.jpg")); 
                break;
            case "Pasig Blvd. Ext.": 
                stationImage.setImage(new Image("file:src/places/pasig.jpg")); 
                break;
            case "SM Marikina": 
                stationImage.setImage(new Image("file:src/places/marikina.jpg")); 
                break;
            case "EDSA Terminal": 
                stationImage.setImage(new Image("file:src/places/mandaluyong.jpg")); 
                break;
            case "Gateway Mall": 
                stationImage.setImage(new Image("file:src/places/gateway.jpg")); 
                break;
            case "SM Fairview": 
                stationImage.setImage(new Image("file:src/places/fairview.jpg")); 
                break;
            default:
                stationImage.setImage(null); 
                break;
        }
    }
    
    /**
     * Displays location information on the UI.
     * 
     * @param stationName The name of the station.
     * @param address The address of the station.
     */
    public void displayLocationInfo(String stationName, String address) {
        Platform.runLater(() -> {
            locationName.setText(stationName);
            locationName.setWrapText(true);
            locationAddress.setText(address);
            locationAddress.setWrapText(true);
            
            if (!isImageSet) {
                stationLogo.setImage(new Image("file:src/images/station.png"));
                addressLogo.setImage(new Image("file:src/images/address.png"));
                isImageSet = true;
            }
            
            setStationImage(stationName);
        });
    }
    
    /**
     * Sets the initial source and destination locations in the ComboBoxes.
     * 
     * @param source The source location.
     * @param destination The destination location.
     */
    public void setInitialLocations(String source, String destination) {
        if (currLocation != null && trgtLocation != null) {
            Location sourceLocation = findLocationByName(source);
            Location destinationLocation = findLocationByName(destination);
            currLocation.setValue(sourceLocation);
            trgtLocation.setValue(destinationLocation);
        } else {
            System.out.println("currLocation or trgtLocation is null.");
        }
    }
    
    /**
     * Helper method that finds a Location object by its name.
     * 
     * @param name The name of the location.
     * @return The Location object if found, otherwise null.
     */
    private Location findLocationByName(String name) {
        return currLocation.getItems().stream()
                .filter(location -> location.getStation().equals(name))
                .findFirst().orElse(null);
    }
        
    // Event handler for selecting the current location from ComboBox.
    public void getCurrentLocation(ActionEvent event) {
        Location selectedLocation = currLocation.getValue();
        if (selectedLocation != null) {
            srcLocation = selectedLocation.getStation();
            setLocations(srcLocation, destLocation);
        }
    }
    
    // Event handler for selecting the target location from ComboBox.
    public void getTargetLocation(ActionEvent event) {
        Location selectedLocation = trgtLocation.getValue();
        if (selectedLocation != null) {
            destLocation = selectedLocation.getStation();
            setLocations(srcLocation, destLocation);
        }
    }
    
    // Method to set the nodeMap and edges from Main class
    public void setGraphData(Map<String, Node> nodeMap, List<Edge> edges) {
        this.nodeMap = Main.getNodeMap();
        this.edges = Main.getEdges();
    }

    /**
     * Sets the initial source and destination locations.
     * 
     * @param source The source location name.
     * @param destination The destination location name.
     */
    public void setLocations(String source, String destination) {
        this.srcLocation = source;
        this.destLocation = destination;
        System.out.println("Source Location: " + source);
        System.out.println("Destination Location: " + destination);

        calculateAndDrawPath();
    }

    /**
     * Calculates and draws the path using the A* algorithm between 
     * the selected source and destination nodes.
     */
    private void calculateAndDrawPath() {
        Node sourceNode = nodeMap.get(srcLocation);
        Node destNode = nodeMap.get(destLocation);

        if (sourceNode != null && destNode != null) {
            List<Node> path = AStar.findPath(sourceNode, destNode, edges);
            path.forEach(System.out::println);
            System.out.println("-".repeat(30));

            // Check if vboxContent is not null before accessing it
            if (vboxContent != null) {
                vboxContent.getChildren().clear();

                // Prepare the path data for JavaScript
                JSONArray pathArray = new JSONArray();

                for (int i = 0; i < path.size(); i++) {
                    Node node = path.get(i);
                    Node pastNode = null;

                    JSONObject point = new JSONObject();
                    point.put("loc", node.getLocation());
                    point.put("lat", node.getLatitude());
                    point.put("lng", node.getLongitude());
                    pathArray.put(point);

                    // Create a new pane for each node
                    if (i > 0) {
                        VBox jeepStop = new VBox();
                        jeepStop.setSpacing(10);
                        jeepStop.setAlignment(Pos.CENTER);

                        Label sourceDestinationText = new Label();
                        Label jeepFareText = new Label();
                        Label edgeDistanceText = new Label();

                        pastNode = path.get(i - 1);
                        sourceDestinationText.getStyleClass().add("source-destination");
                        sourceDestinationText.setText(pastNode.getLocation() + " || " + node.getLocation());

                        double fare = getFare(pastNode, node);
                        int roundedFare = (int) fare;
                        jeepFareText.getStyleClass().add("jeep-fare");
                        jeepFareText.setText("₱" + roundedFare);

                        edgeDistanceText.getStyleClass().add("edge-distance");
                        edgeDistanceText.setText("Distance: " + getDistanceToNextNode(pastNode, node) + " km");

                        jeepStop.getStyleClass().add("jeep-stop");
                        jeepStop.getChildren().addAll(sourceDestinationText, jeepFareText, edgeDistanceText);
                        vboxContent.getChildren().add(jeepStop);
                    }
                }

                System.out.println("Path data: " + pathArray.toString());

                if (webEngine != null) {
                    webEngine.executeScript("drawPath('" + pathArray.toString() + "');");
                } else {
                    System.out.println("WebEngine is not initialized.");
                }
            } else {
                System.out.println("vboxContent is null.");
            }

        } else {
            System.out.println("Source or destination node not found.");
        }
    }

    /**
     * Calculates the fare between two nodes based on distance.
     *
     * @param sourceNode The source node.
     * @param destNode   The destination node.
     * @return The calculated fare.
     */
    private double getFare(Node sourceNode, Node destNode) {
        for (Edge edge : edges) {
            if (edge.getSource().equals(sourceNode) && edge.getDestination().equals(destNode)) {
                double distance = edge.getDistance();
                if (edge.getDistance() > 4) {
                    double fare = ((distance - 4) * 1.8) + 13;
                    return fare;
                } else{
                    double fare = 13;
                    return fare;
                }
            }
        }
        return 0.0;
    }

     /**
     * Retrieves the distance between two nodes.
     *
     * @param sourceNode The source node.
     * @param destNode   The destination node.
     * @return The distance between the two nodes.
     */
    private double getDistanceToNextNode(Node sourceNode, Node destNode) {
        for (Edge edge : edges) {
            if (edge.getSource().equals(sourceNode) && edge.getDestination().equals(destNode)) {
                return edge.getDistance();
            }
        }
        return 0;
    }
    
    /**
     * Switches the scene to the LandingPage view.
     * Loads LandingPage.fxml and sets up the controller with graph data.
     *
     * @param event The action event triggering the scene switch.
     * @throws IOException If an error occurs while loading the FXML file.
     */
    public void switchToLandingPage(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("LandingPage.fxml"));
        root = loader.load();

        System.out.println("Loaded LandingPage.fxml");

        Controller controller = loader.getController();
        controller.setGraphData(nodeMap, edges);
        stage = (Stage)((javafx.scene.Node)event.getSource()).getScene().getWindow();
        Image appIcon = new Image("file:src/images/icon.jpeg");
        stage.getIcons().add(appIcon);
        scene = new Scene(root);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();

        System.out.println("Switched to LandingPage scene");
    }

    /**
     * Switches the scene to the MapPage view.
     * Loads MapPage.fxml and sets up the controller with graph data.
     * Initializes WebView and loads geopositions JSON data for station pins on the map.
     *
     * @param event The action event triggering the scene switch.
     * @throws IOException If an error occurs while loading the FXML file.
     */
    public void switchToMapPage(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MapPage.fxml"));
        root = loader.load();

        System.out.println("Loaded MapPage.fxml");

        Controller controller = loader.getController();
        controller.setGraphData(nodeMap, edges);

        stage = (Stage)((javafx.scene.Node)event.getSource()).getScene().getWindow();
        Image appIcon = new Image("file:src/images/icon.jpeg");
        stage.getIcons().add(appIcon);
        scene = new Scene(root);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();

        System.out.println("Switched to MapPage scene");
        
        WebEngine webEngine = controller.getWebEngine();
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                // Load geopositions JSON and pass to JavaScript with clickable flag
                try {
                    String jsonContent = new String(Files.readAllBytes(Paths.get("src/backend/geopositions.json")));
                    webEngine.executeScript("window.setStationPins(" + jsonContent + ");");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (newState == Worker.State.FAILED) {
                System.out.println("Failed to load WebView: " + webEngine.getLocation());
                webEngine.getLoadWorker().getException().printStackTrace();
            }
        });
    }

    /**
     * Switches the scene to the GetRoute view.
     * Loads GetRoute.fxml and sets up the controller with graph data.
     *
     * @param event The action event triggering the scene switch.
     * @throws IOException If an error occurs while loading the FXML file.
     */
    public void switchGetRoutePage(ActionEvent event) throws IOException {
        Location currentSource = (currLocation != null && currLocation.getValue() != null) ? currLocation.getValue() : new Location("DefaultSource", "");
        Location currentDestination = (trgtLocation != null && trgtLocation.getValue() != null) ? trgtLocation.getValue() : new Location("DefaultDestination", "");

        String currentSourceName = currentSource.getStation();
        String currentDestinationName = currentDestination.getStation();
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("GetRoute.fxml"));
        root = loader.load();

        System.out.println("Loaded GetRoute.fxml");

        Controller controller = loader.getController();
        controller.setGraphData(nodeMap, edges);
        controller.setInitialLocations(currentSourceName, currentDestinationName);

        stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        Image appIcon = new Image("file:src/images/icon.jpeg");
        stage.getIcons().add(appIcon);
        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/frontend/style.css").toExternalForm());
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();

        WebView webView = controller.getWebView();
        if (webView != null) {
            WebEngine webEngine = webView.getEngine();
            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    System.out.println("WebView loaded: " + webEngine.getLocation());
                    controller.setLocations(currentSourceName, currentDestinationName);
                } else if (newState == Worker.State.FAILED) {
                    System.out.println("Failed to load WebView: " + webEngine.getLocation());
                    webEngine.getLoadWorker().getException().printStackTrace();
                }
            });
        } else {
            System.out.println("WebView is null.");
        }

        System.out.println("Switched to GetRoute scene");
    }
    
}