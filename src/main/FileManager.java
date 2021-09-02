package main;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

// 파일 관련 처리하는 클래스
public class FileManager {

	@SuppressWarnings("rawtypes")
	public static void saveFile(File file) {
		String outputSrc = file.getPath();		// 저장경로

		try (FileOutputStream os = new FileOutputStream(outputSrc);
			 ObjectOutputStream oos = new ObjectOutputStream(os)) {
			List<List> serializedSlides = serialize(SlideManager.getSlides());
			oos.writeObject(serializedSlides);					// Write
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public static void openFile(File file) {
		try (FileInputStream is = new FileInputStream(file);
			 ObjectInputStream ois = new ObjectInputStream(is)) {

			Object inputObject = ois.readObject();									// 파일 read

			LinkedList<Pane> slides = deserialize((List<List<SerializedNode>>) inputObject);	// deserialize

			SlideManager.setSlides(slides);
			if(slides.size() != 1) SlideManager.changeSlideTo(1);
			else SlideManager.changeSlideTo(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void exportToImage(File selectedDir) {
		List<WritableImage> exportedImages = new ArrayList<>();			// 추출한 이미지를 담는 리스트

		List<Pane> slides = SlideManager.getSlides();

		// 이미지 추출
		for(Pane slide : slides) {
			exportedImages.add(slide.snapshot(new SnapshotParameters(), null));
		}

		// "Parrot_현재시각"을 이름으로 하는 폴더 생성 후 이미지 저장
		if(selectedDir != null) {
			// 폴더명 설정
			String currentTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
			String outputSrc = selectedDir.getPath() + "/" + "Parrot_" +currentTime;

			// 폴더 및 이미지 파일 생성, 저장
			File newDir = new File(outputSrc);
			if(newDir.mkdir()) {
				try {
					for(int i=1; i < exportedImages.size(); i++) {
						// 파일명 설정
						File img = new File(outputSrc + "/" + "slide_" + i + ".png");
						// 이미지 저장
						ImageIO.write(SwingFXUtils.fromFXImage(exportedImages.get(i), null), "png", img);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public static List<List> serialize(LinkedList<Pane> slides) {
		List<List> serializedSlides = new ArrayList<>();

		slides.stream()
				.skip(1)
				.forEach(slide -> {
					List<SerializedNode> sList = new ArrayList<>();
					// 각각의 Node를 SerializedNode로 치환
					slide.getChildren().stream()
							.skip(1)
							.filter(n -> !(n instanceof NodeManager.ResizeBox))
							.forEach(n -> {
								SerializedNode sNode = new SerializedNode(n);
								sList.add(sNode);
							});
					serializedSlides.add(sList);
				});

		return serializedSlides;
	}

	public static LinkedList<Pane> deserialize(List<List<SerializedNode>> serializedSlides) {
		LinkedList<Pane> deserializedSlides = new LinkedList<>();

		// SerilaizedList를 Pane으로 치환
		for(List<SerializedNode> sList : serializedSlides) {
			Pane slide = SlideManager.makeSlide();

			// SerializedNode를 Node로 치환
			for (SerializedNode sNode : sList) {
				Node deserializedNode = null;						// deserialize가 끝난 후의 결과인 Node를 담는 변수

				// sNode의 정보를 바탕으로 노드 재구성
				switch (sNode.typeOfNode) {
					// Text
					case SerializedNode.TEXT:
						if(sNode.userData != null)
							deserializedNode = NodeManager.createText(
									sNode.text, 0,
									Color.rgb(sNode.red, sNode.green, sNode.blue), (String) sNode.userData);
						else
							deserializedNode = NodeManager.createText(
									sNode.text, 0,
									Color.rgb(sNode.red, sNode.green, sNode.blue));

						deserializedNode.setStyle(sNode.style);
						break;

					// Image
					case SerializedNode.IMAGE:
						try {
							// byte[]를 이미지로 변환
							ByteArrayInputStream bais = new ByteArrayInputStream(sNode.image);
							BufferedImage img = ImageIO.read(bais);

							deserializedNode = NodeManager.createImage(
									SwingFXUtils.toFXImage(img, null), sNode.width, sNode.height);
						} catch(Exception e) {
							e.printStackTrace();
						}
						break;

					// Line
					case SerializedNode.LINE:
						deserializedNode = NodeManager.createLine(
								sNode.startEndXY[0], sNode.startEndXY[1], sNode.startEndXY[2], sNode.startEndXY[3],
								Color.rgb(sNode.red, sNode.green, sNode.blue));
						break;

					// SVGPath(Arrow, Star)
					case SerializedNode.SVG_PATH:
						deserializedNode = new SVGPath();
						((SVGPath) deserializedNode).setContent(sNode.content);
						NodeManager.makeDraggable(deserializedNode);
						break;

					// Ellipse
					case SerializedNode.ELLIPSE:
						deserializedNode = NodeManager.createEllipse(
								sNode.width / 2, sNode.height / 2,
								Color.rgb(sNode.red, sNode.green, sNode.blue));
						break;

					// Rectangle
					case SerializedNode.RECTANGLE:
						deserializedNode = NodeManager.createRectangle(
								sNode.width, sNode.height,
								Color.rgb(sNode.red, sNode.green, sNode.blue));
						break;

					// Polygon
					case SerializedNode.POLYGON:
						deserializedNode = NodeManager.createPolygon(
								sNode.sides, sNode.width, sNode.height,
								Color.rgb(sNode.red, sNode.green, sNode.blue));
						break;
				}

				if(deserializedNode != null) {
					deserializedNode.setLayoutX(sNode.layoutX);
					deserializedNode.setLayoutY(sNode.layoutY);
					deserializedNode.setRotate(sNode.rotate);

					slide.getChildren().add(deserializedNode);
				}
			}

			deserializedSlides.add(slide);
		}



		return deserializedSlides;
	}

	///////////
	// Class //
	///////////

	// Serializable 인터페이스(Object를 파일로 read, write 할 수 있도록 해줌)를 구현하지 않는
	// fx의 Node들을 생성하는 데에 필요한 최소한의 정보를 담는 클래스 (Node 하나에 해당)
	private static class SerializedNode implements Serializable {

		////////////
		// Fields //
		////////////

		// 간헐적으로 발생하는 로드 오류를 잡기 위한 상수
		private static final long serialVersionUID = -3189973179604487089L;
		
		// Node 타입 구분 상수
		private static final int TEXT = 1;
		private static final int IMAGE = 2;
		private static final int LINE = 3;
		private static final int SVG_PATH = 4;
		private static final int ELLIPSE = 5;
		private static final int RECTANGLE = 6;
		private static final int POLYGON = 7;

		private int typeOfNode = -1;

		// Node들에 공통적으로 해당되는 정보
		private double layoutX;
		private double layoutY;
		private double rotate;

		// Text에 해당되는 정보
		private String text;
		private String style;		// style에 textSize 지정되어 있음
		private Object userData;

		// Image에 해당되는 정보
		private byte[] image;

		// Line에 해당되는 정보
		private double[] startEndXY;	// startXY, endXY

		// SVGPath(화살표, 별)에 해당되는 정보
		private String content;

		// Polygon에 해당되는 정보
		private int sides;

		// 그 이외
		private double width;			// Ellipse, Rectangle, Polygon
		private double height;			// Ellipse, Rectangle, Polygon

		private int red;				// 색상 red 값
		private int green;				// 색상 green 값
		private int blue;				// 색상 blue 값

		/////////////
		// Methods //
		/////////////

		// 생성자
		public SerializedNode(Node node) {
			setNodeInfo(node);

			if(node instanceof Label) setLabelInfo((Label) node);
			else if(node instanceof HBox) setImageViewInfo((HBox) node);
			else if(node instanceof Line) setLineInfo((Line) node);
			else if(node instanceof SVGPath) setSVGPathInfo((SVGPath) node);
			else if(node instanceof Ellipse) setEllipseInfo((Ellipse) node);
			else if(node instanceof Rectangle) setRectangleInfo((Rectangle) node);
			else if(node instanceof Pane) setPolygonInfo((Pane) node);

		}

		// Node들에 공통적으로 해당되는 정보 set
		private void setNodeInfo(Node node) {
			layoutX = node.getLayoutX();
			layoutY = node.getLayoutY();
			rotate = node.getRotate();
		}

		// Label에 해당되는 정보 set
		private void setLabelInfo(Label label) {
			typeOfNode = TEXT;
			text = label.getText();
			if(label.getUserData() != null)
				userData = label.getUserData();
			style = label.getStyle();

			// 색상 정보 저장
			Color color = (Color) label.getTextFill();
			red = (int) (color.getRed() * 255);			// Color red값
			green = (int) (color.getGreen() * 255);		// Color green값
			blue = (int) (color.getBlue() * 255);		// Color blue값

		}

		// HBox(이미지 컨테이너)에 해당되는 정보 set
		private void setImageViewInfo(HBox hBox) {
			try {
				typeOfNode = IMAGE;
				ImageView imageView = (ImageView)hBox.getChildren().get(0);

				// 이미지를 byte[]로 변환
				BufferedImage bImg = SwingFXUtils.fromFXImage(imageView.getImage(), null);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(bImg, "png", baos);

				image  = baos.toByteArray();
				width = hBox.getWidth();
				height = hBox.getHeight();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void setLineInfo(Line line) {
			typeOfNode = LINE;

			startEndXY = new double[] {
					line.getStartX(),
					line.getStartY(),
					line.getEndX(),
					line.getEndY()
			};

			// 색상 정보 저장
			Color color = (Color) line.getStroke();
			red = (int) (color.getRed() * 255);			// Color red값
			green = (int) (color.getGreen() * 255);		// Color green값
			blue = (int) (color.getBlue() * 255);		// Color blue값

		}

		private void setSVGPathInfo(SVGPath svgPath) {
			typeOfNode = SVG_PATH;

			content = svgPath.getContent();

			// 색상 정보 저장
			Color color = (Color) svgPath.getFill();
			red = (int) (color.getRed() * 255);			// Color red값
			green = (int) (color.getGreen() * 255);		// Color green값
			blue = (int) (color.getBlue() * 255);		// Color blue값
		}

		private void setEllipseInfo(Ellipse ellipse) {
			typeOfNode = ELLIPSE;

			width = ellipse.getRadiusX() * 2;
			height = ellipse.getRadiusY() * 2;

			// 색상 정보 저장
			Color color = (Color) ellipse.getFill();
			red = (int) (color.getRed() * 255);			// Color red값
			green = (int) (color.getGreen() * 255);		// Color green값
			blue = (int) (color.getBlue() * 255);		// Color blue값
		}

		private void setRectangleInfo(Rectangle rectangle) {
			typeOfNode = RECTANGLE;

			width = rectangle.getWidth();
			height = rectangle.getHeight();

			// 색상 정보 저장
			Color color = (Color) rectangle.getFill();
			red = (int) (color.getRed() * 255);			// Color red값
			green = (int) (color.getGreen() * 255);		// Color green값
			blue = (int) (color.getBlue() * 255);		// Color blue값
		}

		private void setPolygonInfo(Pane container) {
			typeOfNode = POLYGON;

			Polygon polygon = (Polygon) container.getChildren().get(0);

			width = container.getWidth();
			height = container.getHeight();
			sides = polygon.getPoints().size() / 2;

			// 색상 정보 저장
			Color color = (Color) polygon.getFill();
			red = (int) (color.getRed() * 255);			// Color red값
			green = (int) (color.getGreen() * 255);		// Color green값
			blue = (int) (color.getBlue() * 255);		// Color blue값
		}
	}
}