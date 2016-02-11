/* Graphit - log file browser
 * Copyright© 2015 Shamil Absalikov, foxling@live.com
 *
 * Graphit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graphit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.foxling.graphit;

import java.awt.Dimension;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;

import ru.foxling.graphit.config.ConfigModel;
import ru.foxling.graphit.config.DataType;
import ru.foxling.graphit.config.Field;
import ru.foxling.graphit.config.FieldRole;
import ru.foxling.graphit.config.FieldValue;
import ru.foxling.graphit.logfile.LogFile;
import ru.foxling.graphit.logfile.Record;
import ru.foxling.graphit.logfile.Startup;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JTable;

import java.awt.FlowLayout;

import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;

import java.awt.event.InputEvent;

import javax.swing.JTextField;
import javax.swing.JPopupMenu;
import java.awt.Component;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JCheckBox;

import java.awt.Font;
import java.awt.Color;
import net.miginfocom.swing.MigLayout;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;

import java.awt.BorderLayout;

public class MainFrame
extends JFrame implements ChartProgressListener {
	private static final long serialVersionUID = 1L;
	private static final String APPNAME = "Graphit - ИСУ \"Оптима\" ";
	private static final Logger LOG = Logger.getLogger(MainFrame.class.getName());
	
	private JPanel contentPane;
	private JMenu mFile = null;
	private LogFile logFile = null;
	//private Chart chart = null;
	private ChartPanel chartPanel = null;
	
	private JMenuItem miDetails;
	private JSplitPane splitPane;
	private JScrollPane spTable;
	private JTable table;
	/** Protection against Chart~Table onChange cycle.</br>
	 * *Chart onClick-event goes before Chart Cursor actually moves, so couldn't make it with onClick only */
	private boolean chartMSequence = false;
	private boolean doChartTrack = true;
	private boolean doTableTrack = true;
	private JTextField tfCurrFile;
	private JPopupMenu popupMenu;
	private JCheckBoxMenuItem miWrongHashOnly;
	private JMenu mRecent;
	private JCheckBox cbLaunch;
	private JCheckBox cbTable;
	private ConfigController configController;
	private JPanel pAxes;
	private JMenu mSettings;
	private JMenu mYAxes;
	private JMenuItem miPreferences;
	
	
	public MainFrame() {
		super(APPNAME);
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 850, 491);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		mFile = new JMenu("Файл");
		menuBar.add(mFile);
		
		final JMenuItem miOpen = new JMenuItem("Открыть");
		miOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final JFileChooser fc = new JFileChooser();
				if (fc.showOpenDialog(miOpen)==JFileChooser.APPROVE_OPTION){
					openLogFile(fc.getSelectedFile());
				}
			}
		});
		ClassLoader classLoader = getClass().getClassLoader();
		miOpen.setIcon(new ImageIcon(classLoader.getResource("ic_action_collection.png")));
		miOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		mFile.add(miOpen);
		
		mRecent = new JMenu("Последние файлы");
		mRecent.setEnabled(false);
		mFile.add(mRecent);
		
		mFile.addSeparator();
		
		miDetails = new JMenuItem("Детали");
		miDetails.setEnabled(false);
		miDetails.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Details fDetails = new Details(logFile);
				fDetails.setVisible(true);
			}
		});
		miDetails.setIcon(new ImageIcon(classLoader.getResource("ic_action_storage.png")));
		miDetails.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));
		mFile.add(miDetails);
		
		mRecent.addSeparator();
		JMenuItem mi = new JMenuItem("Очистить список");
		mi.addActionListener(e -> configController.removeRecentFiles());
		mRecent.add(mi);
		
		mSettings = new JMenu("Опции");
		menuBar.add(mSettings);
		
		mYAxes = new JMenu("Оси Y");
		mSettings.add(mYAxes);
		mSettings.addSeparator();
		
		miPreferences = new JMenuItem("Настройки");
		miPreferences.addActionListener(e -> ConfigFrame.launch());
		miPreferences.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK));
		mSettings.add(miPreferences);
		
		tfCurrFile = new JTextField();
		tfCurrFile.setEditable(false);
		menuBar.add(tfCurrFile);
		tfCurrFile.setColumns(10);
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(1, 1, 1, 1));
		setContentPane(contentPane);
		
	    chartPanel = new ChartPanel(null);//chart.chart
	    chartPanel.setBackground(Color.WHITE);
	    chartPanel.addMouseListener(new MouseAdapter() {
	    	@Override
	    	public void mouseClicked(MouseEvent e) {
	    		chartMSequence = true;
	    	}
	    });
		FlowLayout flowLayout = (FlowLayout) chartPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEADING);
		
		table = new JTable();
		table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		table.setCellSelectionEnabled(true);
		table.setFillsViewportHeight(true);
		
		ListSelectionModel rowSM = table.getSelectionModel();
		rowSM.addListSelectionListener(new ListSelectionListener() {
			@Override
			/** Listener for row-change events (Chart trace) */
			public void valueChanged(ListSelectionEvent e) {
				if (!doTableTrack || !table.isFocusOwner() || e.getValueIsAdjusting()) return;
				
				ListSelectionModel rowSM = (ListSelectionModel) e.getSource();
				int selectedIndex = rowSM.getMinSelectionIndex();
				
				if (chartPanel != null) {
					// TODO
					/*DefaultTableModel model = (DefaultTableModel) table.getModel();
					try {
						Date time = fTime.parse((String) (model.getValueAt(selectedIndex, 1)));
					
						JFreeChart jfreechart = chartPanel.getChart();
						if (jfreechart != null)	{
							XYPlot xyplot = (XYPlot)jfreechart.getPlot();
							xyplot.setDomainCrosshairValue(time.getTime());
						}
					} catch (ParseException e1) {
						e1.printStackTrace();
					}*/
				}
			}
		});
		
		spTable = new JScrollPane(table);
		spTable.setVisible(false);
		
		popupMenu = new JPopupMenu();
		addPopup(table, popupMenu);
		
		miWrongHashOnly = new JCheckBoxMenuItem("Отображать только с неправильным хэшем");
		miWrongHashOnly.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doChartTrack = !miWrongHashOnly.isSelected();
				fillTable(miWrongHashOnly.isSelected());
			}
		});
		popupMenu.add(miWrongHashOnly);
		
		
		JPanel toppanel = new JPanel();
		toppanel.setPreferredSize(new Dimension(560,367));
		toppanel.setLayout(new BorderLayout(0, 0));
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));
		
		toppanel.add(chartPanel);
		
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, toppanel, spTable);
	    splitPane.setResizeWeight(1);
	    contentPane.add(splitPane);
		
	    Runtime.getRuntime().addShutdownHook(new Thread(){
			@Override
			public void run()
			{
				Core.getConfigModel().saveConfig();
			}
	    });

		// Create the drag and drop listener
	    CustomDragDropListener customDragDropListener = new CustomDragDropListener();
	    new DropTarget(chartPanel, customDragDropListener);
	    
	    JPanel pTools = new JPanel();
	    pTools.setBackground(Color.WHITE);
	    toppanel.add(pTools, BorderLayout.SOUTH);
	    pTools.setLayout(new MigLayout("insets 0", "[][grow][67px]", "[23px]"));
	    
	    cbLaunch = new JCheckBox("Запуск");
	    pTools.add(cbLaunch, "cell 0 0");
	    //cbLaunch.setEnabled(false);
	    /*
	    cbLaunch.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
	    		Chart.setCollectionVisible(chartPanel.getChart(), 0, cbLaunch.isSelected());
	    	}
	    });*/
	    cbLaunch.setBackground(Color.WHITE);
	    cbLaunch.setForeground(Color.BLACK);
	    cbLaunch.setFont(new Font("Tahoma", Font.BOLD, 11));
	    
	    pAxes = new JPanel();
	    pAxes.setBackground(Color.WHITE);
	    FlowLayout flowLayout_1 = (FlowLayout) pAxes.getLayout();
	    flowLayout_1.setAlignment(FlowLayout.LEFT);
	    flowLayout_1.setVgap(0);
	    pTools.add(pAxes, "cell 1 0,grow");
	    
	    cbTable = new JCheckBox("Таблица");
	    cbTable.setEnabled(false);
	    cbTable.setBackground(Color.WHITE);
	    pTools.add(cbTable, "cell 2 0,alignx right,aligny top");
	    
	    configController = new ConfigController(Core.getConfigModel());
	    // TODO End of MainFrame.Constructor
	}
	
	private void openLogFile(File aFile){
		logFile = new LogFile(aFile.getPath());
		try {
			logFile.readFile();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Не удалось прочитать файл " + aFile.getName(), "Ошибка", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return;
		}
		tfCurrFile.setText(logFile.getFileName());
			
		StringBuilder titleBuilder = new StringBuilder(APPNAME);
		
		if (logFile.getStartups().size() > 0 && logFile.getStartups().get(0).getDate() != null)
			titleBuilder.append(" Дата: ").append(logFile.getStartups().get(0).getDate().format(Core.F_DATE)).append(", ");
		
		if (logFile.getSerialNo() != null && !logFile.getSerialNo().isEmpty())
			titleBuilder.append("Сер.№ ").append(logFile.getSerialNo()).append(", ");
		
		if (logFile.getFrimware() != null && !logFile.getFrimware().isEmpty())
			titleBuilder.append("ПО ").append(logFile.getFrimware());
		
		String title = titleBuilder.toString();
		if (title.endsWith(", ")) title = title.substring(0, title.length() - 3);
		this.setTitle(title);
		
		fillTable();
		
		miDetails.setEnabled(true);
		cbLaunch.setEnabled(true);
		cbTable.setEnabled(true);
		
		configController.refreshAxesList();
		configController.addRecentFile(aFile.getPath());
		chartPanel.setChart(Chart.chartFactory(logFile));
	}
	
	private void setTableVisible(boolean visible) {
		spTable.setVisible(visible);
		if (visible) {
			splitPane.setDividerLocation(.75);
		} else {
			splitPane.setDividerLocation(1);
		}
	}
	
	private void fillTable(){
		fillTable(false);
	}
	
	private void fillTable(boolean wrongLinesOnly){
		doTableTrack = false;
		table.setModel(new LogFileTableModel(logFile, wrongLinesOnly));
		List<Field> fieldList = Core.getConfigModel().getFieldList();
		Enumeration<TableColumn> cols = table.getColumnModel().getColumns();
		while (cols.hasMoreElements()) {
			TableColumn col = (TableColumn) cols.nextElement();
			Field field = fieldList.get(col.getModelIndex());
			if (!field.getValueList().isEmpty())
				col.setCellRenderer(new FieldValueRenderer(field));
		}
		doTableTrack = true;
	}
	
	class CustomDragDropListener
	implements DropTargetListener {
		@Override
		public void drop(DropTargetDropEvent dtde) {
			dtde.acceptDrop(DnDConstants.ACTION_COPY);
	        Transferable t = dtde.getTransferable();
	        if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
	            try {
	                Object td = t.getTransferData(DataFlavor.javaFileListFlavor);
	                if (td instanceof List) {
	                	@SuppressWarnings("rawtypes")
						Object value = ((List) td).get(0);
	                	if (value instanceof File) {
                            File file = (File) value;
                            openLogFile(file);
                        }
	                }
	            } catch (UnsupportedFlavorException | IOException ex) {
	                ex.printStackTrace();
	            }
	        }
	        
	        dtde.dropComplete(true);
		}
		
		@Override public void dragEnter(DropTargetDragEvent dtde) {}
		@Override public void dragOver(DropTargetDragEvent dtde) {}
		@Override public void dropActionChanged(DropTargetDragEvent dtde) {}
		@Override public void dragExit(DropTargetEvent dte) {}
	}
	
	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}

	@Override
	public void chartProgress(ChartProgressEvent arg0) {
		if (arg0.getType() != 2 || !chartMSequence) return;
		chartMSequence = false;
		
		if (doChartTrack && chartPanel != null) {
			JFreeChart jfreechart = chartPanel.getChart();
			if (jfreechart != null)	{
				XYPlot xyplot = (XYPlot)jfreechart.getPlot();
				double d = xyplot.getDomainCrosshairValue();

				Date time = new Date((long) d);
				/*int i = lf.getId(time);
				if (i>-1) {
					Rectangle rect = table.getCellRect(i, 0, true);
					table.scrollRectToVisible(rect);					
					table.setRowSelectionInterval(i,i);
					table.setColumnSelectionInterval(1, 1);
				}*/
			}
		}
	}
	
	private static class Chart {
		//TODO Chart class
		private static JFreeChart chart;
		private static XYPlot plot;
		private static LogFile logFile;
		private static Field xField;
		private static List<Field> yFields = new LinkedList<>();
		
		public static JFreeChart chartFactory(LogFile logFile) {
			yFields.clear();
			if (logFile == null) {
				LOG.log(Level.SEVERE, "logFile is NULL");
				return null;
			}
			Chart.logFile = logFile;
			
			TimeSeriesCollection dsLaunch = new TimeSeriesCollection();
			TimeSeries tsLaunch = new TimeSeries("Launch");
			for (Startup startup : logFile.getStartups()) {
				LocalDateTime date = startup.getDatetime();
				tsLaunch.addOrUpdate(new Second(date.getSecond(), date.getMinute(), date.getHour(), date.getDayOfMonth(), date.getMonthValue(), date.getYear()), 0);
			}
			dsLaunch.addSeries(tsLaunch);
			chart = ChartFactory.createTimeSeriesChart("Chart Title", "xAxisLabel", "yAxisLabel", dsLaunch, false, false, false);
			plot = chart.getXYPlot();
		    plot.setBackgroundPaint(Color.white);
	        plot.setDomainGridlinePaint(Color.lightGray);
	        plot.setRangeGridlinePaint(Color.lightGray);
	        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
	        plot.setDomainCrosshairVisible(true);
	        plot.setDomainCrosshairLockedOnData(true);
	        
	        ValueAxis axis = plot.getRangeAxis();
	        axis.setVisible(false);
	        
	        // TimeSeries count (Launches count)
	        int tsCount = dsLaunch.getSeriesCount();
	        
	        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
	        if (tsCount != 0)
	        	renderer.setSeriesShapesVisible(0, false);
	        renderer.setSeriesShapesFilled(0, true);
	    	renderer.setSeriesShapesVisible(0, true);
	    	renderer.setSeriesLinesVisible(0, false);
	    	renderer.setSeriesPaint(0, Color.black);
	    	
	        plot.setRenderer(0, renderer);
	        
	        List<Field> yFields = new LinkedList<>();
			for (Field f : Core.getConfigModel().getFieldList()) {
				if (f.getRole() == FieldRole.X_AXIS) {
					xField = f;
				} else if (f.getRole() == FieldRole.DRAW)
					yFields.add(f);
			}
			
			if (xField != null)
				for (Field yField : yFields) {
					plotFactory(yField);
				}
			
	        return chart;
		}
		
		public static void drawField(Field field) {
			if (field == null)
				return;
			
			if (yFields.indexOf(field) == -1) {
				plotFactory(field);
			} else
				setFieldVisible(field, true);
		}
		
		public static void setFieldVisible(Field field, boolean visible) {
			if (field == null)
				return;
			int id = yFields.indexOf(field);
			if (id == -1)
				return;
			
			NumberAxis axis = (NumberAxis) plot.getRangeAxis(id);
			axis.setVisible(visible);
			
			XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer(id);
			
			for (int i = 0; i < plot.getDataset(id).getSeriesCount(); i++) {
				if (id != 0)
					renderer.setSeriesLinesVisible(i, visible);
				
				if (id == 0 && i != 0) {
					renderer.setSeriesShapesVisible(i, visible);
				}
			}
		}
		
		private static boolean plotFactory(Field yField) {
			if (xField == null) {
				LOG.log(Level.SEVERE, "Ось X не определена");
				return false;
			}
			if (yField == null) {
				LOG.log(Level.SEVERE, "Ось Y не определена");
				return false;
			}
			
			int xFieldId = Core.getConfigModel().getFieldList().indexOf(xField),
				yFieldId = Core.getConfigModel().getFieldList().indexOf(yField);
			
			if (xFieldId == -1 || yFieldId == -1) {
				LOG.log(Level.SEVERE, "Поля для графика не определились ({0}={1}; {2}={3})", new Object[]{ xField, xFieldId, yField, yFieldId });
				return false;
			}
			
			TimeSeriesCollection collection = new TimeSeriesCollection();
			for (Startup startup : logFile.getStartups()) {
				TimeSeries timeSeries = new TimeSeries(yField.getName());
				for (Record rec : startup.getRecords()) {
					if (rec.isDirty()) continue;
					Object fieldValue = rec.getValue(xFieldId);
					if (fieldValue == null) continue;
					if (fieldValue instanceof LocalTime) {
						LocalTime time = (LocalTime) fieldValue;
						fieldValue = rec.getValue(yFieldId);
						if (fieldValue == null) continue;
						double value = objectToDouble(fieldValue);
						if (value != Double.NaN)
							timeSeries.addOrUpdate(
									new Second(time.getSecond(),
												time.getMinute(),
												time.getHour(), 1, 1, 1900),
									value
							);
					}
				}
				if (!timeSeries.isEmpty())
					collection.addSeries(timeSeries);
			}
			
			NumberAxis numberAxis = new NumberAxis(yField.getName());
	        numberAxis.setAutoRangeIncludesZero(false);
	        numberAxis.setLabelPaint(Color.red);
	        numberAxis.setInverted(true);
	        numberAxis.setAutoRangeStickyZero(false);
	        numberAxis.setVisible(true);
	        
	        int id = yFields.size();
	        yFields.add(yField);
	        plot.setRangeAxis(id, numberAxis);
	        plot.setRangeAxisLocation(id, AxisLocation.BOTTOM_OR_LEFT);
	        
	        XYDataset xydsDepth = collection;
	        plot.setDataset(id, xydsDepth);
	        plot.mapDatasetToRangeAxis(id, id);
	        
	        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
	        for (int i = 0; i < collection.getSeriesCount(); i++) {
	        	renderer.setSeriesLinesVisible(i, true);
	        	renderer.setSeriesPaint(i, Color.red);
	        	renderer.setSeriesShapesVisible(i, false);
	        	renderer.setSeriesShapesFilled(i, false);
			}
	        plot.setRenderer(id, renderer);
	        return true;
		}
		
		private static double objectToDouble(Object value) {
			if (value instanceof Byte)
				return ((Byte) value).doubleValue();
			if (value instanceof Short)
				return ((Short) value).doubleValue();
			if (value instanceof Integer)
				return ((Integer) value).doubleValue();
			if (value instanceof Float)
				return ((Float) value).doubleValue();
			if (value instanceof Double)
				return ((Double) value).doubleValue();
			return Double.NaN;
		}
	}

	
	private class ConfigController {
		private ConfigModel configModel;
		private ArrayList<Link> links;
		
		private final Color[] colors = {
				Color.RED, Color.GREEN, Color.BLUE,
				Color.BLACK, Color.YELLOW, Color.CYAN,
				Color.PINK, Color.MAGENTA
		};
		
		private final int[] KEY_LIST = {
				KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3
				, KeyEvent.VK_4, KeyEvent.VK_5, KeyEvent.VK_6
				, KeyEvent.VK_7, KeyEvent.VK_8, KeyEvent.VK_9
				, KeyEvent.VK_0
		};
		
		private ActionListener alOpenRecent = e -> {
			File file = new File(((JMenuItem) e.getSource()).getText());
			if (file.exists()) {
				openLogFile(file);
			} else {
				Object[] options = {"Удалить из списка",
	                    "Удалить из списка все некорректные ссылки",
	                    "Ничего не делать"};
				int n = JOptionPane.showOptionDialog(null,
				    "Файл [" + file.getPath() +"] не найден.",
				    "Файл не найден",
				    JOptionPane.YES_NO_CANCEL_OPTION,
				    JOptionPane.QUESTION_MESSAGE,
				    null,
				    options,
				    options[0]);
				
				if (n == 0) {
					removeRecentFile(file.getPath());
				} else 
					if (n == 1)
						for (String recent : configModel.getRecentFiles())
							if (!new File(recent).exists())
								configModel.removeRecentFile(recent);
			}
		};
		
		private ActionListener alToggleYAxis = e -> {
			JCheckBoxMenuItem mi = (JCheckBoxMenuItem) e.getSource();
			Link link = getLink(mi);
			if (link == null || link.field == null) return;
			configModel.toggleYAxis(link.field, mi.isSelected());
		};
		
		public ConfigController(ConfigModel configModel) {
			// TODO ConfigController
			this.configModel = configModel;
			links = new ArrayList<>();
			configModel.addPropertyListener(e -> {
				switch (e.getPropertyName()) {
				case "recent-file":
					updateRecentMenu();
					break;
				case "launch-visible":
					cbLaunch.setSelected(configModel.getLaunchVisible());
					break;
				case "table-visible":
					boolean visible = configModel.getTableVisible();
					cbTable.setSelected(visible);
					setTableVisible(visible);
					break;
				}
			});
			
			configModel.addFieldListener(e -> {
				Field field = (Field) e.getSource(); 
				if (field != null) {
					switch (e.getPropertyName()) {
					case "role":
						if (field.getRole() == FieldRole.DRAW) {
							Chart.drawField(field);
						} else
							Chart.setFieldVisible(field, false);
						break;
					case "color":
						break;
					}
				}
			});
			
			cbLaunch.setSelected(configModel.getLaunchVisible());
			cbLaunch.addActionListener(e -> {
				JCheckBox cb = (JCheckBox)e.getSource(); 
				configModel.setLaunchVisible(cb.isSelected());
			});
			

			setTableVisible(configModel.getTableVisible());
			cbTable.setSelected(configModel.getTableVisible());
			cbTable.addActionListener(e -> {
				JCheckBox cb = (JCheckBox)e.getSource(); 
				configModel.setTableVisible(cb.isSelected());
			});
			
			updateRecentMenu();
		}
		
		/*public Link getLink(Field field) {
			if (field == null)
				return null;
			
			for (Link link : links) {
				if (link.field.equals(field))
					return link;
			}
			
			return null;
		}*/
		
		public Link getLink(JCheckBoxMenuItem menuItem) {
			if (menuItem == null)
				return null;
			
			for (Link link : links) {
				if (link.yMenuItem.equals(menuItem))
					return link;
			}
			
			return null;
		}
		
		/** Refreshes Axes list at the "Add Axis" button's popup menu*/
		public void refreshAxesList() {
			mYAxes.removeAll();
			links.clear();
			
			List<Field> fields = Core.getConfigModel().getFieldList(); 
			for (Field field : fields) {
				if (field.getDatatype() == DataType.STRING)
					continue;
				Link link = new Link(field);
				links.add(link);
				mYAxes.add(link.yMenuItem);
				link.yMenuItem.addActionListener(alToggleYAxis);
			}
		}
		
		public void addRecentFile(String path) {
			configModel.addRecentFile(path);
		}
		
		public void removeRecentFile(String path) {
			configModel.removeRecentFile(path);
		}
		
		public void removeRecentFiles() {
			configModel.removeRecentFiles();
		}
		
		private void updateRecentMenu() {
			int size = mRecent.getItemCount() - 2;
			LinkedList<String> paths = configModel.getRecentFiles(); 
			
			// Equalize counts of menu items and files in set
			for (int i = size; i > paths.size(); i--)
				mRecent.remove(paths.size());
			
			for (int i = size; i < paths.size(); i++) {
				JMenuItem mi = new JMenuItem();
				if (KEY_LIST.length > i) { 
					mi.setAccelerator(KeyStroke.getKeyStroke(KEY_LIST[i], InputEvent.ALT_MASK));
				} else
					mi.setAccelerator(null);
				mi.addActionListener(alOpenRecent);
				mRecent.add(mi, i);
			}
			
			// Update the captions
			for (int i = 0; i < paths.size(); i++) {
				JMenuItem mi = mRecent.getItem(i);
				mi.setText(paths.get(i));
			}
			
			mRecent.setEnabled(paths.size() > 0);
		}
		
		private Color getColor(int i) {
			if (colors.length > i) {
				return colors[i];
			} else
				return Color.BLACK;
		}
	
		private class Link {
			public final Field field;
			public final JCheckBoxMenuItem yMenuItem;
			public Color color;
			
			public Link(Field field) {
				this.field = field;
				color = getColor(links.size());
				
				yMenuItem = new JCheckBoxMenuItem(field.getName());
				yMenuItem.setForeground(color);
				yMenuItem.setFont(new Font("Tahoma", Font.BOLD, 11));
				yMenuItem.setSelected(field.getRole() == FieldRole.DRAW);
			}
		}
	}
	
	static class FieldValueRenderer
	extends DefaultTableCellRenderer {
	    private static final long serialVersionUID = 4264832765857567868L;
	    private Field field;
	    
		public FieldValueRenderer(Field field) {
			super();
			this.field = field;
		}

	    public void setValue(Object value) {
	    	if (value == null) {
	    		setText("");
	    		return;
	    	}
	    	setText(value.toString());
	    	
	    	if (!field.isBitmask()) {
	    		for (FieldValue fValue : field.getValueList())
	    			if (fValue.value.equals(value)) {
	    				if (fValue.caption != null) setText(fValue.caption);
	    				if (fValue.description != null) setToolTipText(fValue.source + ": " + fValue.description);
						return;
					}
	    	} else {
				try {
					StringBuilder result = new StringBuilder();
					int iVal = objectToInt(value);
					if (iVal == 0)
						return;
					
					for (FieldValue fValue : field.getValueList()) {
						if (fValue.value.equals(value)) {
							if (fValue.caption != null) setText(fValue.caption);
		    				if (fValue.description != null) setToolTipText(fValue.source + ": " + fValue.description);
							return;
						}
						
						int ifVal = objectToInt(fValue.value);
						if ((iVal & ifVal) > 0 && fValue.description != null && !fValue.description.isEmpty())
							result.append(fValue.source).append(": ").append(fValue.description).append("<br>");
					}

					if (result.length() != 0) {
						setToolTipText(result.insert(0, "<html>").append("</html>").toString());
					}
				} catch (Exception e) {
					LOG.log(Level.WARNING, "Ошибка при попытке собрать всплывающую подсказку", e);
					setText("");
					return;
				}
			}
	    }

	    /** Converts object representation of a whole number to int.
		 * @param num the object
		 * @return <code>intValue()</code> or <code>0</code> */
		private int objectToInt(Object num) {
			if (num == null)
				return 0;
			
			if (num instanceof Byte) {
				return ((Byte) num).intValue();
			} else if (num instanceof Short) {
				return ((Short) num).intValue();
			} else if (num instanceof Integer) {
				return ((Integer) num).intValue();
			}
			
			return 0;
		}
	    
	}
	
	static class LogFileTableModel
	extends AbstractTableModel {
		private static final long serialVersionUID = -6341608314922452350L;
		private List<Field> fieldList;
		
		/** Records index */
		private ArrayList<Record> index;
		
		public LogFileTableModel(LogFile logFile, boolean wrongLinesOnly) {
			super();
			fieldList = Core.getConfigModel().getFieldList();
			index = new ArrayList<Record>(25);
			for (Startup startup : logFile.getStartups())
				for(Record record: startup.getRecords())
					if (!record.isDirty())
						index.add(record);
		}
		
		@Override
		public int getRowCount() {
			return index.size();
		}

		@Override
		public int getColumnCount() {
			return fieldList.size();
		}
		
		@Override
		public String getColumnName(int columnIndex) {
			return fieldList.get(columnIndex).getName();
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return fieldList.get(columnIndex).getDatatype().get_class();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return index.get(rowIndex).getValue(columnIndex);
		}
		
		@Override
	    public boolean isCellEditable(int row, int column) {
	       return false;
	    }		
	}
}