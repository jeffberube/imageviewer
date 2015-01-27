/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cs3010_lab3_imageviewer;

import java.io.File;
import java.io.FileFilter;
import java.util.Iterator;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 *
 * @author francoc
 */
public class CS3010_Lab3_ImageViewer extends Application {
    
    private final int SCENE_HEIGHT = 720;
    private final int SCENE_WIDTH = 1080;
    private final int CONTROL_AREA_WIDTH = 96;
    
    private boolean busy_flag = false;
    private boolean drag_flag = false;
    
    private double initialMouseX = 0;
    private double initialScrollPaneHvalue = 0;
    
    private Stage stage;
    private Scene scene;
    private StackPane rootPane;
    private ScrollPane scrollPane;
    private BorderPane controlPane;
    private ImageStrip imageStrip;
    private File[] pictures;
    
    @Override
    public void start(Stage primaryStage) {
        
        stage = primaryStage;
        
        stage.setTitle("The Only Image Viewer You'll Ever Need");
        stage.setScene(createScene());
        stage.show();
        
    }
    
    private File[] readPictures() {
        
        File dir = new File(".");
        File[] pictures = dir.listFiles(new ImageFilter());
        
        return pictures;
    
    }
    
    private Scene createScene() {
        
        Scene scene = new Scene(createRootPane(), SCENE_WIDTH, SCENE_HEIGHT);
        scene.getStylesheets().add("cs3010_lab3_imageviewer/style.css");
        
        scene.setOnKeyPressed(e->{
        
            switch(e.getCode()) {
            
                case LEFT:  initialScrollPaneHvalue = scrollPane.getHvalue();
                            previousImage();
                            break;
                    
                case RIGHT: initialScrollPaneHvalue = scrollPane.getHvalue();
                            nextImage();
                            break;
                
            }
            
        });

        scene.setOnMousePressed(e->{ 
            
            if (scrollPane.getHvalue() == 0) {
                
                scrollPane.setHvalue(scrollPane.getHmax() - ((double)(1.0 / (pictures.length + 1))));
                initialScrollPaneHvalue = scrollPane.getHvalue();
            
            } else if (scrollPane.getHvalue() >= scrollPane.getHmax() - ((double)(1.0 / (pictures.length + 1)))) {
                
                scrollPane.setHvalue(0);
                initialScrollPaneHvalue = 0;        
            
            } else
                initialScrollPaneHvalue = scrollPane.getHvalue();
            
            initialMouseX = e.getX();
            
        });
        
        scene.setOnMouseDragged(e->{

            drag_flag = true;
            scrollPane.setHvalue(initialScrollPaneHvalue + ((1.0 / imageStrip.getWidth()) * (initialMouseX - e.getX())));
   
        });
        
        scene.setOnMouseReleased(e->{
            
            if (drag_flag) {

                if (e.getX() > initialMouseX + (scene.getWidth() / 3))
                    previousImage();
                else if (e.getX() < initialMouseX - (scene.getWidth() / 3))
                    nextImage();
                else
                    returnImage();
                
                drag_flag = false;
            
            }
            
        });

        scene.widthProperty().addListener(e-> {
            
            Iterator imageIterator = imageStrip.getChildren().iterator();
            
            while(imageIterator.hasNext())
                ((ImageView)imageIterator.next()).setFitWidth(scene.getWidth() - (CONTROL_AREA_WIDTH * 2));
            
            imageStrip.setMinWidth(scene.getWidth() * (pictures.length + 2));
            scrollPane.setHvalue(initialScrollPaneHvalue);
            
        });
        
        scene.heightProperty().addListener(e-> {
            
            Iterator imageIterator = imageStrip.getChildren().iterator();
            
            while(imageIterator.hasNext())
                ((ImageView)imageIterator.next()).setFitHeight(scene.getHeight());
            
            imageStrip.setMaxHeight(scene.getHeight());
            
        });
        
        this.scene = scene;
        return scene;
        
    }
    
    private void returnImage() {
        
        if (!busy_flag) {
            
            busy_flag = true;
        
            final Timeline timeline = new Timeline();
            timeline.setCycleCount(1);

            final KeyValue kv = new KeyValue(scrollPane.hvalueProperty(), 
                        initialScrollPaneHvalue, Interpolator.EASE_BOTH);

            final KeyFrame kf = new KeyFrame(Duration.millis(150), kv);

            timeline.getKeyFrames().add(kf);
            timeline.play();

            timeline.setOnFinished(eh-> {
                
                busy_flag = false;

            });
        
        }
    
    }
    
    private StackPane createRootPane() {
    
        StackPane rootPane = new StackPane();
        rootPane.getStyleClass().add("rootpane");

        rootPane.getChildren().add(createScrollPane());
        rootPane.getChildren().add(createControlPane());
        
        rootPane.applyCss();
        
        this.rootPane = rootPane;
        return rootPane;
        
    }
    
    private ScrollPane createScrollPane() {
    
        ScrollPane scrollPane = new ScrollPane();
        
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        scrollPane.setFitToHeight(true);
        scrollPane.setContent(createImageStrip());
        
        scrollPane.setHvalue((double)(1.0 / (pictures.length + 1)));
        
        initialScrollPaneHvalue = scrollPane.getHvalue();
        
        this.scrollPane = scrollPane;
        return scrollPane;
        
    }
    
    private HBox createImageStrip() {
    
        pictures = readPictures();
        ImageStrip imageStrip = new ImageStrip();

        imageStrip.setFillHeight(true);
        imageStrip.setMinWidth(SCENE_WIDTH * (pictures.length + 2));
        imageStrip.setMaxHeight(SCENE_HEIGHT);
        imageStrip.setSpacing(CONTROL_AREA_WIDTH * 2);
        imageStrip.setAlignment(Pos.CENTER);
        imageStrip.getStyleClass().add("rootpane");
        
        imageStrip.populate(pictures, SCENE_HEIGHT, SCENE_WIDTH - (CONTROL_AREA_WIDTH * 2));

        this.imageStrip = imageStrip;
        return imageStrip;
        
    }
    
    private BorderPane createControlPane() {
        
        BorderPane controlPane = new BorderPane();
        controlPane.setRight(createNextButton());
        controlPane.setLeft(createPrevButton());
        controlPane.setPadding(new Insets(16, 16, 16, 16));
        
        this.controlPane = controlPane;
        return controlPane;
    
    }
    
    private RoundButton createNextButton() {
    
        Polygon forward = new Polygon();
        forward.getPoints().addAll(new Double[] {5.0, 5.0, 30.0, 20.0, 5.0, 35.0});
        forward.setFill(Color.DARKGREY);
        forward.setTranslateX(2);
        
        RoundButton next = new RoundButton(32, forward);
        
        next.setOnMouseClicked(e->{ nextImage(); });
        
        return next;
        
    }
    
    private void nextImage() {
        
        if (!busy_flag) {
            
            busy_flag = true;
            
            if (scrollPane.getHvalue() >= scrollPane.getHmax() - ((double)(1.0 / (pictures.length + 1)))) {
                scrollPane.setHvalue(0);
                initialScrollPaneHvalue = 0;        
            }
        
            final Timeline timeline = new Timeline();
            timeline.setCycleCount(1);

            final KeyValue kv = new KeyValue(scrollPane.hvalueProperty(), 
                        initialScrollPaneHvalue + ((double)(1.0 / (pictures.length + 1))), Interpolator.EASE_BOTH);

            final KeyFrame kf = new KeyFrame(Duration.millis(150), kv);

            timeline.getKeyFrames().add(kf);
            timeline.play();

            timeline.setOnFinished(eh-> { 
                
                initialScrollPaneHvalue = scrollPane.getHvalue();
                
                busy_flag = false; 
            });
        
        }
        
    }
    
    private RoundButton createPrevButton() {
    
        Polygon backward = new Polygon();
        backward.getPoints().addAll(new Double[] {30.0, 5.0, 5.0, 20.0, 30.0, 35.0});
        backward.setFill(Color.DARKGREY);
        backward.setTranslateX(-5);
        
        RoundButton prev = new RoundButton(32, backward);
        
        prev.setOnMouseClicked(e->{ previousImage(); });
        
        return prev;
        
    }
    
    private void previousImage() {
        
        if (!busy_flag) {
            
            busy_flag = true;
            
            if (scrollPane.getHvalue() == 0) {
                scrollPane.setHvalue(scrollPane.getHmax() - ((double)(1.0 / (pictures.length + 1))));
                initialScrollPaneHvalue = scrollPane.getHvalue();
            }
        
            final Timeline timeline = new Timeline();
            timeline.setCycleCount(1);

            final KeyValue kv = new KeyValue(scrollPane.hvalueProperty(), 
                        initialScrollPaneHvalue - ((double)(1.0 / (pictures.length + 1))), Interpolator.EASE_BOTH);

            final KeyFrame kf = new KeyFrame(Duration.millis(150), kv);

            timeline.getKeyFrames().add(kf);
            timeline.play();

            timeline.setOnFinished(eh-> { 
                
                initialScrollPaneHvalue = scrollPane.getHvalue();
                
                busy_flag = false; 
            
            });
        
        }
        
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    
    
}

class ImageFilter implements FileFilter{     
    
    @Override
    public boolean accept(File pathname) {
        
        String name = pathname.getName();
        if (name==null) return false;
        return name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".gif");
        
    }
    
}

class ImageStrip extends HBox {

    public ImageStrip() {
    
    }
    
    public void addImage(Image image, int fitWidth, int fitHeight) {
    
        ImageView imageView = new ImageView(image);
            
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(fitHeight);
        imageView.setFitWidth(fitWidth);
            
        getChildren().add(imageView);
        
    }
    
    public void populate(File[] pictures, int fitWidth, int fitHeight) {
    
        if (pictures.length == 0) {
        
            Label noPictures = new Label("No pictures to display.");
            getChildren().add(noPictures);
        
        } else {

            // Create first picture for loop
            Image firstImage = new Image("file:" + pictures[pictures.length - 1].getName());
            addImage(firstImage, fitWidth, fitHeight);

            // Create main strip
            for (File picture: pictures) {

                Image image = new Image("file:" + picture.getName());
                addImage(image, fitWidth, fitHeight);

            }

            // Create last picture for loop
            Image lastImage = new Image("file:" + pictures[0].getName());
            addImage(lastImage, fitWidth, fitHeight);

        }

    }
    
}

class RoundButton extends StackPane {

    private Circle couter;
    private Circle cinner;
    
    public RoundButton(int radius, Shape content) {
        
        super();
    
        couter = new Circle();
        couter.setRadius(radius);
        couter.setFill(Color.rgb(255, 255, 255, 0.5));
        
        cinner = new Circle();
        cinner.setRadius(radius - 4);
        cinner.setFill(Color.WHITE);

        this.getChildren().add(couter);
        this.getChildren().add(cinner);
        this.getChildren().add(content);
        
        this.setOnMouseEntered(e-> {
            couter.setFill(Color.rgb(95, 162, 217));
            content.setFill(Color.rgb(95, 162, 217));
        });
        
        this.setOnMouseExited(e-> {
            couter.setFill(Color.rgb(255, 255, 255, 0.5));
            content.setFill(Color.DARKGREY);
        });
        
    }
    
}