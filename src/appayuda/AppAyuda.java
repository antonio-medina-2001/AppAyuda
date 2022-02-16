package appayuda;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Callback;
import netscape.javascript.JSObject;

public class AppAyuda extends Application {
    // Variables globales
    Scene scene;
    
    @Override
    public void start(Stage stage) {
        stage.setTitle("Web View");
        scene = new Scene(new Browser(), 750,500,Color.GREY);
        stage.setScene(scene);
        //scene.getStylesheets().add(App.class.getResource("BrowserToolbar.css").toExternalForm());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    class Browser extends Region 
    {
        // Greamos un tool bar para navegar por diferentes paginas
        private HBox toolBar;
        private final String[] imageFiles = new String[]{"images/product.png","images/blog.png","images/documentation.png","images/partners.png", "images/help.png"};
        private final String[] captions = new String[]{"Products","Blogs","Documentation","Partners", "Ayuda"};
        private final String[] urls = new String[]{
        "http://www.http://blogs.oracle.com/oracle.com/products/index.html",
        "http://blogs.oracle.com/",
        "http://docs.oracle.com/javase/index.html",
        "http://www.oracle.com/partners/index.html ",
        AppAyuda.class.getResource("help.html").toExternalForm()};

        // Instanciamos arrays
        final ImageView selectedImage = new ImageView();
        final Hyperlink[] hpls = new Hyperlink[captions.length];
        final Image[] images = new Image[imageFiles.length];
        private boolean needDocumentationButton = false;

        // VAriables
        final WebView browser = new WebView();
        final WebEngine webEngine = browser.getEngine();
        
        final Button showPrevDoc = new Button("Toggle Previous Docs");
        
        final Button toggleHelpTopics = new Button("Toggle Help Topics");
        final WebView smallView = new WebView();



        // Constructor
        public Browser() 
        {
            //apply the styles
            //getStyleClass().add("browser");
            //Para tratar lo tres enlaces
            for (int i = 0; i < captions.length; i++) 
            {
                Hyperlink hpl = hpls[i] = new Hyperlink(captions[i]);
                Image image = images[i] = new Image(getClass().getResourceAsStream(imageFiles[i]));
                hpl.setGraphic(new ImageView (image));
                final String url = urls[i];
                final boolean addButton = (hpl.getText().equals("Ayuda"));

                //proccess event
                hpl.setOnAction(new EventHandler<ActionEvent>() 
                {
                    @Override
                    public void handle(ActionEvent e) 
                    {
                        needDocumentationButton = addButton;

                        webEngine.load(url);
                    }
                });
            }
            // creamos el toolbar
            toolBar = new HBox();
            toolBar.setAlignment(Pos.CENTER);
            //toolBar.getStyleClass().add("browser-toolbar");
            toolBar.getChildren().addAll(hpls);
            toolBar.getChildren().add(createSpacer());
            
            //set action for the button
            toggleHelpTopics.setOnAction(new EventHandler() 
            {
                @Override
                public void handle(Event t) 
                {
                    webEngine.executeScript("toggle_visibility('help_topics')");
                }
            });
            
            smallView.setPrefSize(120, 80);
            //handle popup windows
            webEngine.setCreatePopupHandler(new Callback<PopupFeatures, WebEngine>() 
            {
                @Override 
                public WebEngine call(PopupFeatures config) 
                {
                    smallView.setFontScale(0.8);
                    if (!toolBar.getChildren().contains(smallView)) 
                    {
                        toolBar.getChildren().add(smallView);
                    }
                    return smallView.getEngine();
                }
            });


            // process page loading
            webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() 
            {
                @Override
                public void changed(ObservableValue<? extends State> ov, State oldState, State newState) 
                {
                    toolBar.getChildren().remove(toggleHelpTopics);
                    if (newState == State.SUCCEEDED) 
                    {
                        JSObject win = (JSObject) webEngine.executeScript("window");
                        win.setMember("app", new JavaApp());
                        if (needDocumentationButton) 
                        {   
                            toolBar.getChildren().add(toggleHelpTopics);
                        }
                    }
                }
            });
            
            // load the web page
            webEngine.load("http://aula.ieslosmontecillos.es");
            //add components
            getChildren().add(toolBar);

            //add the web view to the scene
            getChildren().add(browser);
        }
        // JavaScript interface object
        public class JavaApp {
            public void exit() 
            {
                Platform.exit();
            }
        }
        
        private Node createSpacer() 
        {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            return spacer;
        }

        @Override
        protected void layoutChildren() 
        {
            double w = getWidth();
            double h = getHeight();
            double tbHeight = toolBar.prefHeight(w);
            layoutInArea(browser,0,0,w,h-tbHeight,0, HPos.CENTER, VPos.CENTER);
            layoutInArea(toolBar,0,h-tbHeight,w,tbHeight,0,HPos.CENTER,VPos.CENTER);
        }

        @Override
        protected double computePrefWidth(double height) 
        {
            return 750;
        }

        @Override
        protected double computePrefHeight(double width) 
        {
            return 500;
        }
    }
}