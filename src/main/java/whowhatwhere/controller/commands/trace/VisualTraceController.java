/*******************************************************************************
 * Who What Where
 * Copyright (C) 2017  ck3ck3
 * https://github.com/ck3ck3/WhoWhatWhere
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package whowhatwhere.controller.commands.trace;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import whowhatwhere.controller.GUIController;

public class VisualTraceController implements Initializable
{
	@FXML
	private ImageView imgView;
	@FXML
	private Button btnClose;
	@FXML
	private Label labelLoading;
	@FXML
	private Pane paneTraceInfo;
	@FXML
	private AnchorPane anchorPaneScreen;
	@FXML
	private SplitPane splitPane;
	@FXML
	private ScrollPane scrollPane;
	@FXML
	private StackPane paneStackColor;
	
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{
		GUIController.setCommonGraphicOnLabeled(btnClose, GUIController.CommonGraphicImages.OK);
	}
	
	public ScrollPane getScrollPane()
	{
		return scrollPane;
	}

	public SplitPane getSplitPane()
	{
		return splitPane;
	}
	
	public AnchorPane getAnchorPaneScreen()
	{
		return anchorPaneScreen;
	}
	
	public Pane getPaneTraceInfo()
	{
		return paneTraceInfo;
	}
	
	public ImageView getImgView()
	{
		return imgView;
	}

	public Button getBtnClose()
	{
		return btnClose;
	}

	public Label getLoadingLabel()
	{
		return labelLoading;
	}

	public StackPane getPaneStackColor()
	{
		return paneStackColor;
	}
}
