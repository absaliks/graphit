/* graphit - log file browser
 * Copyright© 2015 Shamil Absalikov, foxling@live.com
 *
 * graphit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * graphit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ru.foxling.graphit;

import java.awt.EventQueue;
import java.util.EventObject;
import java.util.List;
import java.util.function.BiPredicate;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.AbstractListModel;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.border.TitledBorder;
import javax.swing.JTabbedPane;
import java.awt.FlowLayout;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import net.miginfocom.swing.MigLayout;
import ru.foxling.graphit.config.ConfigModel;
import ru.foxling.graphit.config.DataType;
import ru.foxling.graphit.config.Field;
import ru.foxling.graphit.config.FieldDelimiter;
import ru.foxling.graphit.config.FieldEvent;
import ru.foxling.graphit.config.FieldRole;
import ru.foxling.graphit.config.FieldValue;
import ru.foxling.graphit.config.Format;

import java.awt.Color;
import javax.swing.BoxLayout;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;

import java.awt.Dimension;
import javax.swing.JPopupMenu;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.time.LocalTime;

import javax.swing.JMenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Insets;

public class ConfigFrame extends JFrame {
	private static final long serialVersionUID = 3103016344816004897L;
	private JPanel contentPane;
	private JTextField iFieldName;
	private JComboBox<FieldDelimiter> iFieldDelimiter;		
	private JComboBox<DataType> iDataType;
	private JComboBox<Format> iFormat;
	private FieldListModel mdlFieldList;
	private JList<Field> iFieldList;
	private JMenuItem miAddField;
	private JMenuItem miRemoveField;
	private JMenuItem miAddValue;
	private JMenuItem miRemoveValue;
	private JTextField edtFormat;
	private JCheckBox iOptional;
	private JTable tValues;
	private JScrollPane spValues;
	private ConfigModel configModel;
	private FormatListModel mdlFormatlist;
	private ValueListModel mdlValueList;
	private byte[] configState;
	private JButton btnCancel;
	private JButton btnOkay;
	private JComboBox<FieldDelimiter> iDefaultFieldDelimiter;
	private JComboBox<FieldDelimiter> iDefaultLineDelimiter;
	private JRadioButton iWorkDir;
	private JRadioButton iCurrUser;
	private JCheckBox iBitMask;
	private JCheckBox iHashsum;
	private JComboBox<FieldRole> iFieldRole;
	private JButton iColorChooser;
	
	public ConfigFrame() {
		super("Настройки");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 600, 500);
		
		configModel = Core.getConfigModel();
		saveConfigState();
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(1, 1, 1, 1));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		contentPane.add(tabbedPane, BorderLayout.CENTER);
		
		JPanel pnlGeneral = new JPanel();
		tabbedPane.addTab("Общее", null, pnlGeneral, null);
		pnlGeneral.setLayout(new BorderLayout(0, 0));
		
		JPanel pnlConfigLocation = new JPanel();
		pnlConfigLocation.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		pnlGeneral.add(pnlConfigLocation, BorderLayout.NORTH);
		pnlConfigLocation.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
		
		JLabel lblConfigLocation = new JLabel("Хранилище настроек:");
		lblConfigLocation.setVerticalAlignment(SwingConstants.BOTTOM);
		pnlConfigLocation.add(lblConfigLocation);
		
		iWorkDir = new JRadioButton("Рабочая папка");
		iWorkDir.setSelected(true);
		pnlConfigLocation.add(iWorkDir);
		
		iCurrUser = new JRadioButton("Папка текущего пользователя");
		pnlConfigLocation.add(iCurrUser);
		
		ButtonGroup groupConfigLocation = new ButtonGroup();
	    groupConfigLocation.add(iWorkDir);
	    groupConfigLocation.add(iCurrUser);
	    
	    
	    JPanel panel = new JPanel();
	    panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Значения по умолчанию", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
	    pnlGeneral.add(panel, BorderLayout.CENTER);
	    panel.setLayout(new MigLayout("", "[][grow]", "[][]"));
	    
	    JLabel label = new JLabel("Разделитель столбцов");
	    panel.add(label, "cell 0 0,alignx trailing");
	    
	    iDefaultFieldDelimiter = new JComboBox<FieldDelimiter>(new DefaultComboBoxModel<FieldDelimiter>(FieldDelimiter.values()));
	    panel.add(iDefaultFieldDelimiter, "cell 1 0,growx");
	    
	    JLabel lblNewLabel = new JLabel("Разделитель строк");
	    panel.add(lblNewLabel, "cell 0 1,alignx trailing");
	    
	    iDefaultLineDelimiter = new JComboBox<FieldDelimiter>(new DefaultComboBoxModel<FieldDelimiter>(FieldDelimiter.values()));
	    panel.add(iDefaultLineDelimiter, "cell 1 1,growx");
		
		JPanel pnlFields = new JPanel();
		tabbedPane.addTab("Настройка полей", null, pnlFields, null);
		pnlFields.setLayout(new MigLayout("", "[][]", "[]"));
		
		JPanel pnlFieldList = new JPanel();
		pnlFieldList.setPreferredSize(new Dimension(100, 100));
		pnlFields.add(pnlFieldList, "cell 0 0,grow");
		pnlFieldList.setLayout(new BoxLayout(pnlFieldList, BoxLayout.X_AXIS));
		
		JScrollPane spFieldList = new JScrollPane();
		pnlFieldList.add(spFieldList);
		
		iFieldList = new JList<Field>();
		spFieldList.setViewportView(iFieldList);
		
		JPopupMenu pmFieldList = new JPopupMenu();
		addPopup(iFieldList, pmFieldList);
		
		miAddField = new JMenuItem("Добавить");
		pmFieldList.add(miAddField);
		
		miRemoveField = new JMenuItem("Удалить");
		pmFieldList.add(miRemoveField);
		
		JPanel pnlMisc = new JPanel();
		pnlFields.add(pnlMisc, "cell 1 0,grow");
		
		
		JLabel lblFieldName = new JLabel("Имя");
		JLabel lblFieldDelimiter = new JLabel("Ограничитель столбца");
		JLabel lblDataType = new JLabel("Тип данных");
		JLabel lblOptional = new JLabel("Необязательное");
		JLabel lblFormat = new JLabel("Формат");
		JPanel pnlFormat = new JPanel();
		pnlFormat.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		
		iFieldName = new JTextField();
		iFieldDelimiter = new JComboBox<FieldDelimiter>();
		iDataType = new JComboBox<DataType>();
		iOptional = new JCheckBox();
		iFormat = new JComboBox<Format>();
		edtFormat = new JTextField();
		pnlFormat.setLayout(new BoxLayout(pnlFormat, BoxLayout.Y_AXIS));
		pnlFormat.add(iFormat);
		pnlFormat.add(edtFormat);
		
		pnlMisc.setLayout(new MigLayout("", "[grow][grow]", "[][][][][][][][][grow]"));
		pnlMisc.add(lblFieldName, "cell 0 0");
		pnlMisc.add(iFieldName, "cell 1 0,growx");
		pnlMisc.add(lblFieldDelimiter, "cell 0 1");
		pnlMisc.add(iFieldDelimiter, "cell 1 1,growx");
		pnlMisc.add(lblDataType, "cell 0 2");
		pnlMisc.add(iDataType, "cell 1 2,growx");
		pnlMisc.add(lblFormat, "cell 0 3");
		pnlMisc.add(pnlFormat, "cell 1 3,grow");
		
		JLabel lblRole = new JLabel("Роль");
		pnlMisc.add(lblRole, "cell 0 4");
		
		iFieldRole = new JComboBox<FieldRole>(new DefaultComboBoxModel<>(FieldRole.values()));
		pnlMisc.add(iFieldRole, "cell 1 4,growx,split 2");
		
		iColorChooser = new JButton();
		iColorChooser.setMinimumSize(new Dimension(18, 18));
		iColorChooser.setMaximumSize(new Dimension(18, 18));
		iColorChooser.setMargin(new Insets(0, 0, 0, 0));//new Insets(8, 8, 7, 7));
		pnlMisc.add(iColorChooser);
		pnlMisc.add(lblOptional, "cell 0 6");
		pnlMisc.add(iOptional, "cell 1 6,growx");
		
		JLabel lblHashsum = new JLabel("Хэш-сумма");
		pnlMisc.add(lblHashsum, "cell 0 7");
		
		iHashsum = new JCheckBox();
		iHashsum.setEnabled(false);
		pnlMisc.add(iHashsum, "cell 1 7");
		
		JPanel pnlValues = new JPanel();
		pnlValues.setBorder(new TitledBorder(null, "Набор значений", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		pnlMisc.add(pnlValues, "cell 0 8 2 1,grow");
		pnlValues.setLayout(new BorderLayout(0, 0));
		
		spValues = new JScrollPane();
		pnlValues.add(spValues);
		
		tValues = new JTable() {
			private static final long serialVersionUID = 7567104525794311551L;
			private String[] columnToolTips = {
					"Значение из лог файла",
					"Короткое пояснение, отображается в таблице",
					"Длинное описание, отображается в всплывающей подсказке таблицы"};
			//Implement table header tool tips.
			protected JTableHeader createDefaultTableHeader() {
				return new JTableHeader(columnModel) {
					private static final long serialVersionUID = 5363995066011938534L;

					public String getToolTipText(MouseEvent e) {
						java.awt.Point p = e.getPoint();
						int index = columnModel.getColumnIndexAtX(p.x);
						int realIndex = columnModel.getColumn(index).getModelIndex();
						return columnToolTips[realIndex];
					}
				};
			}
		};
		spValues.setViewportView(tValues);
		
		JPopupMenu pmValueList = new JPopupMenu();
		addPopup(spValues, pmValueList);
		addPopup(tValues, pmValueList);
		
		miAddValue = new JMenuItem("Добавить");
		pmValueList.add(miAddValue);
		
		miRemoveValue = new JMenuItem("Удалить");
		pmValueList.add(miRemoveValue);
		
		iBitMask = new JCheckBox("Значения - битовая маска");
		iBitMask.setToolTipText("Значение поля может содержать несколько значений из данного набора");
		pnlValues.add(iBitMask, BorderLayout.NORTH);
		
		JPanel pnlBottom = new JPanel();
		contentPane.add(pnlBottom, BorderLayout.SOUTH);
		pnlBottom.setLayout(new FlowLayout(FlowLayout.RIGHT, 3, 0));
		
		JButton btnLoad = new JButton("Load");
		btnLoad.addActionListener(e -> {
			configModel.loadConfig();
		});
		
		JButton btnEventTest = new JButton("Event Test");
		btnEventTest.addActionListener(e -> {
			JColorChooser cc = new JColorChooser();
		});
		pnlBottom.add(btnEventTest);
		pnlBottom.add(btnLoad);
		
		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(e -> {
			configModel.saveConfig();
		});
		pnlBottom.add(btnSave);
		
		btnOkay = new JButton("ОК");
		pnlBottom.add(btnOkay);
		
		btnCancel = new JButton("Отмена");
		pnlBottom.add(btnCancel);
		
		//TODO
		configController();
	}

	private void saveConfigState(){
		ObjectOutputStream out = null;
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			out = new ObjectOutputStream(stream);
			out.writeObject(configModel);
			configState = stream.toByteArray();
			out.close();
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private void loadConfigState() {
		try {
			ByteArrayInputStream stream = new ByteArrayInputStream(configState);
			ObjectInputStream in = new ObjectInputStream(stream);
			configModel = (ConfigModel) in.readObject();
		} catch(IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		ConfigFrame.launch();
	}
	
	public static void launch() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ConfigFrame frame = new ConfigFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
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
				Component component = e.getComponent(); 
				if (component != null && component.isEnabled())
					popup.show(component, e.getX(), e.getY());
			}
		});
	}
	
	private void configController(){
		mdlFieldList = new FieldListModel();
		iFieldList.setModel(mdlFieldList);
		
		// ConfigModel.onChange
		configModel.addFieldListener(evt -> {
			mdlFieldList.refresh();
		});
		
		// Field List - Context Menu > Add field
		miAddField.addActionListener((ActionEvent arg0) -> {
			configModel.addFieldAfter(getSelectedField());
		});
		
		// Field List - Context Menu > Remove field(-s)
		miRemoveField.addActionListener((ActionEvent arg0) -> {
			List<Field> list = iFieldList.getSelectedValuesList();
			int id = iFieldList.getSelectedIndex(); 
			if (id > 0) {
				iFieldList.setSelectedIndex(id - 1);
			} else
				iFieldList.clearSelection();
			configModel.removeFields(list);
		});
		
		
		iFieldDelimiter.setModel(new DelimiterListModel(configModel));
		
		// ComboBox "Datatype" model
		iDataType.setModel(new DataTypeListModel(configModel));
		
		// ComboBox "Format" model
		mdlFormatlist = new FormatListModel();
		iFormat.setModel(mdlFormatlist);
		
		// Table "Field Values" model
		mdlValueList = new ValueListModel();
		tValues.setModel(mdlValueList);
		
		// Config model >> Field List >> onChange 
		configModel.addFieldListener((evt) -> {
			Field field = (Field) evt.getSource(); 
			if (field == null || field == getSelectedField()) {
				mdlValueList.setField(getSelectedField());
			}
			if (evt.getType() == FieldEvent.UPDATE && field == getSelectedField()) {
				refreshFieldInfo(evt);
			}
		});
		
		// Select field
		iFieldList.addListSelectionListener(e -> refreshFieldInfo(e));

		iFieldDelimiter.addActionListener(e -> {
				Field field = getSelectedField();
				if (field == null)
					return;
				
				if (!iFieldDelimiter.getSelectedItem().equals(field.getDelimiter()))
					configModel.setFieldDelimiter(field, (FieldDelimiter) iFieldDelimiter.getSelectedItem());
		});
		
		iFieldRole.addActionListener(e -> {
			Field field = getSelectedField();
			if (field == null)
				return;
			
			if (!iFieldRole.getSelectedItem().equals(field.getRole()))
				if (!configModel.setFieldRole(field, (FieldRole) iFieldRole.getSelectedItem()))
					iFieldRole.setSelectedItem(field.getRole());
		});

		// FieldName Change
		MyVerifier nameVerifier = new MyVerifier((c, a) -> {
			configModel.setFieldName(getSelectedField(), iFieldName.getText());
			return true;
		});
		iFieldName.setInputVerifier(nameVerifier);
		iFieldName.addActionListener(nameVerifier);
		
		// Format edit Change
		MyVerifier formatVerifier = new MyVerifier((c, a) -> {
			configModel.setFieldFormat(getSelectedField(), edtFormat.getText());
			return true;
		});
		edtFormat.setInputVerifier(formatVerifier);
		edtFormat.addActionListener(formatVerifier);
		
		updateContolState();
		
		miAddValue.addActionListener(e -> {
			Field field = getSelectedField();
			int index = tValues.getSelectedRow();
			FieldValue value = new FieldValue("");
			if (index == -1) {
				configModel.addFieldValueAt(field, null, value);
			} else {
				configModel.addFieldValueAt(field, index + 1, value);
			}
		});
		
		miRemoveValue.addActionListener((e) -> {
			configModel.removeFieldValue(getSelectedField(), tValues.getSelectedRows());
		});
		
		btnOkay.addActionListener(e -> {
			configModel.saveConfig();
			super.dispose();
		});
		
		btnCancel.addActionListener(e -> {
			loadConfigState();
			super.dispose();
		});

		configModel.addPropertyListener(e -> {
			switch (e.getPropertyName()) {
			case "field-delimiter":
				iDefaultFieldDelimiter.setSelectedItem(FieldDelimiter.valueOf(e.getValue()));
				break;
			case "line-delimiter":
				iDefaultLineDelimiter.setSelectedItem(FieldDelimiter.valueOf(e.getValue()));
				break;
			case "config-file-location":
				refreshConfigFileLocation(e.getValue());
			}
		});
		
		iDefaultFieldDelimiter.setSelectedItem(configModel.getDefaultFieldDelimiter());
		iDefaultFieldDelimiter.addActionListener(e -> {
			FieldDelimiter d = (FieldDelimiter) iDefaultFieldDelimiter.getSelectedItem();
			configModel.setProperty("field-delimiter", d.name());
		});
		
		iDefaultLineDelimiter.setSelectedItem(configModel.getDefaultLineDelimiter());
		iDefaultLineDelimiter.addActionListener(e -> {
			FieldDelimiter d = (FieldDelimiter) iDefaultLineDelimiter.getSelectedItem();
			configModel.setProperty("line-delimiter", d.name());
		});
		
		refreshConfigFileLocation(configModel.getConfigFileLocation());
		iWorkDir.addActionListener(e -> {
			if (!configModel.moveConfigFile(ConfigModel.WORKDIR_PATH))
				refreshConfigFileLocation(configModel.getConfigFileLocation());
		});
		iCurrUser.addActionListener(e -> {
			if (!configModel.moveConfigFile(ConfigModel.APPDATA_PATH))
				refreshConfigFileLocation(configModel.getConfigFileLocation());
		});
		
		iOptional.addActionListener(e -> {
			if (!configModel.setFieldOptional(getSelectedField(), iOptional.isSelected()))
				iOptional.setSelected(getSelectedField().isOptional());			
		});
		
		iHashsum.addActionListener(e -> {
			if (!configModel.setFieldHashsum(getSelectedField(), iHashsum.isSelected()))
				iHashsum.setSelected(getSelectedField().isHashsum());			
		});
		
		iBitMask.addActionListener(e -> {
			configModel.setFieldBitmask(getSelectedField(), iBitMask.isSelected());
		});
		
		// TODO
	}
	
	private void refreshConfigFileLocation(String path) {
		if (path == ConfigModel.APPDATA_PATH) {
			iCurrUser.setSelected(true);
		} else if (path == ConfigModel.WORKDIR_PATH)
			iWorkDir.setSelected(true);
	}
	
	/** Updates control state: enabled/disabled, clear values */
	private void updateContolState(){
		Field field = getSelectedField();
		boolean selected = field != null;
		
		iFieldName.setEnabled(selected);
		iFieldDelimiter.setEnabled(selected);
		iDataType.setEnabled(selected);
		iFormat.setEnabled(selected);
		edtFormat.setEnabled(selected);
		iFieldRole.setEnabled(selected);
		iOptional.setEnabled(selected);
		tValues.setEnabled(selected);
		spValues.setEnabled(selected);
		iHashsum.setEnabled(selected);
		
		if (selected) {
			DataType datatype = field.getDatatype();
			if (datatype != null) {
				edtFormat.setEnabled(!datatype.isFixedFormatList());
				iFormat.setEnabled(!(datatype.getFormatList().size() == 0 ||
									datatype.getFormatList().size() == 1 &&
									datatype.getFormatList().get(0).equals(Format.EMPTY_FORMAT)));
				iBitMask.setVisible(datatype == DataType.BYTE || datatype == DataType.SHORT || datatype == DataType.INTEGER);
				iFieldRole.setEnabled(datatype != DataType.STRING);
			}
		}
	}
	
	private Field getSelectedField() {
		return iFieldList.getSelectedValue();
	}
	
	private void refreshFieldInfo(EventObject evt) {
		Field field = getSelectedField();
		if (field != null) {
			iFieldName.setText(field.getName());
			iOptional.setSelected(field.isOptional());
			edtFormat.setText(field.getFormatValue());
			mdlFormatlist.refresh();
			mdlValueList.setField(field);
			iFieldRole.setSelectedItem(field.getRole());
			iBitMask.setSelected(field.isBitmask());
			iHashsum.setSelected(field.isHashsum());
		} else {
			iFieldName.setText("");
			iFieldDelimiter.setSelectedIndex(-1);
			iDataType.setSelectedIndex(-1);
			edtFormat.setText("");
			iFormat.setSelectedIndex(-1);
			iFieldRole.setSelectedIndex(-1);
			iOptional.setSelected(false);
			iBitMask.setSelected(false);
			iHashsum.setSelected(false);
		}
		
		updateContolState();
	}
	
	private class FieldListModel
	extends AbstractListModel<Field> {
		private static final long serialVersionUID = -6222628541194781163L;

		@Override
		public Field getElementAt(int index) {
			return configModel.getFieldList().get(index);
		}

		@Override
		public int getSize() {
			return configModel.getFieldList().size();
		}
		
		/** Refreshes whole list */
		public void refresh() {
			fireContentsChanged(this, -1, -1);
		}
	}
	
	private class DelimiterListModel
	extends AbstractListModel<FieldDelimiter>
	implements ComboBoxModel<FieldDelimiter> {
		private static final long serialVersionUID = -3972948053898882301L;
		private ConfigModel configModel;
		
		public DelimiterListModel(ConfigModel configModel) {
			this.configModel = configModel;
		}
		
		@Override
		public FieldDelimiter getElementAt(int index) {
			return FieldDelimiter.values()[index];
		}

		@Override
		public int getSize() {
			return FieldDelimiter.values().length;
		}

		@Override
		public Object getSelectedItem() {
			Field field = getSelectedField();
			if (field != null)
				return field.getDelimiter();
			
			return null;
		}

		@Override
		public void setSelectedItem(Object object) {
			Object selectedItem = getSelectedItem();
			if (selectedItem == null && object == null ||
				selectedItem != null && selectedItem.equals(object) ||
				object != null && getIndexOf(object) == -1)
					return;
			
			configModel.setFieldDelimiter(getSelectedField(), (FieldDelimiter) object);
			refresh();
		}
		
		/** Refreshes whole list */
		public void refresh() {
			fireContentsChanged(this, -1, -1);
		}
		
		/**
		 * Returns the index of the specified element in the model's item list.
		 * @param object  the element.
		 * @return The index of the specified element in the model's item list or -1 if it's not in the list.
		 * */
		private int getIndexOf(Object object) {
			FieldDelimiter[] values = FieldDelimiter.values();
			for (int i = 0; i < values.length; i++) {
				if (values[i].equals(object))
					return i;
			}
			return -1;
		}
	}
	
	private class DataTypeListModel
	extends AbstractListModel<DataType>
	implements ComboBoxModel<DataType> {
		private static final long serialVersionUID = -3972948053898888801L;
		private ConfigModel configModel;
		
		public DataTypeListModel(ConfigModel configModel) {
			this.configModel = configModel;
		}
		
		@Override
		public DataType getElementAt(int index) {
			return DataType.values()[index];
		}

		@Override
		public int getSize() {
			return DataType.values().length;
		}

		@Override
		public Object getSelectedItem() {
			Field field = getSelectedField();
			if (field != null)
				return field.getDatatype();
			
			return null;
		}

		@Override
		public void setSelectedItem(Object object) {
			Object selectedItem = getSelectedItem();
			if (selectedItem == null && object == null ||
				selectedItem != null && selectedItem.equals(object) ||
				object != null && getIndexOf(object) == -1)
					return;
			
			configModel.setFieldDatatype(getSelectedField(), (DataType) object);
			refresh();
		}
		
		/** Refreshes whole list */
		public void refresh() {
			fireContentsChanged(this, -1, -1);
		}
		
		/**
		 * Returns the index of the specified element in the model's item list.
		 * @param object  the element.
		 * @return The index of the specified element in the model's item list or -1 if it's not in the list.
		 * */
		private int getIndexOf(Object object) {
			DataType[] values = DataType.values();
			for (int i = 0; i < values.length; i++) {
				if (values[i].equals(object))
					return i;
			}
			return -1;
		}
	}
	
	private class FormatListModel
	extends AbstractListModel<Format>
	implements ComboBoxModel<Format> {
		private static final long serialVersionUID = -3972948053898888801L;
		
		@Override
		public Format getElementAt(int index) {
			List<Format> formatList = getFormatList();
			if (formatList != null) {
				return formatList.get(index);
			}
			return null;
		}

		@Override
		public int getSize() {
			List<Format> formatList = getFormatList();
			if (formatList != null) {
				return formatList.size();
			}
			return 0;
		}

		@Override
		public Object getSelectedItem() {
			Field field = getSelectedField();
			if (field != null) {
				return field.getFormat();
			} else
				return null;
		}

		@Override
		public void setSelectedItem(Object object) {
			Format selectedItem = (Format) getSelectedItem();
		if (selectedItem == null && object == null ||
				selectedItem != null && selectedItem.equals(object) ||
				object != null && getIndexOf(object) == -1)
					return;
			
			configModel.setFieldFormat(getSelectedField(), (Format) object);
			refresh();
		}
		
		/** Refreshes whole list */
		public void refresh() {
			fireContentsChanged(this, -1, -1);
		}
		
		/** Returns the model's item list */
		private List<Format> getFormatList() {
			Field field = getSelectedField();
			if (field != null) {
				DataType datatype = field.getDatatype();
				if (datatype != null)
					return datatype.getFormatList();
			}
			return null;
		}
		
		/**
		 * Returns the index of the specified element in the model's item list.
		 * @param format  the element.
		 * @return The index of the specified element in the model's item list.
		 * */
		private int getIndexOf(Object format) {
			List<Format> list = getFormatList();
			if (list != null) {
				return list.indexOf(format);
			} else
				return -1;
		}
	}
	
	private class ValueListModel
	extends AbstractTableModel {
		private static final long serialVersionUID = 3742047021848215242L;
		
		private final String[] COLS = { "Значение", "Метка", "Описание" };
		private final Class<?>[] COL_CLASS = {Object.class, String.class, String.class};
		
		private List<FieldValue> valueList;
		
		@Override
		public int getColumnCount() {
			return COLS.length;
		}
		
		@Override
		public String getColumnName(int col) {
			return COLS[col];
		}
		
		@Override
		public Class<?> getColumnClass(int col) {
			return COL_CLASS[col];
		}

		@Override
		public int getRowCount() {
			if (valueList != null) {
				return valueList.size();
			} else
				return 0;
		}

		@Override
		public Object getValueAt(int row, int col) {
			if (valueList != null) {
				if (row >= valueList.size())
					return null;
				switch (col) {
				case 0:
					return valueList.get(row).value;
				case 1:
					return valueList.get(row).caption;
				case 2:
					return valueList.get(row).description;
				default:
					return null;
				}
			} else
				return null;
		}
		
		@Override
		public void setValueAt(Object value, int row, int col) {
			if (valueList != null) {
				if (row >= valueList.size())
					return;
				switch (col) {
				case 0:
					valueList.get(row).value = value;
					break;
				case 1:
					valueList.get(row).caption = value.toString();
					break;
				case 2:
					valueList.get(row).description = value.toString();
					break;
				}
			}
		}
		
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}
		
		public void setField(Field field) {
			if (field != null) {
				valueList = field.getValueList();
				fireTableDataChanged();
			} else
				if (valueList != null) {
					valueList = null;
					fireTableDataChanged();
				}
		}
	}
	
	/** The format verifier handles both pressing Enter key and Focus loosing */
	class MyVerifier
		extends InputVerifier
	    implements ActionListener {
		
		BiPredicate<JComponent,ActionEvent> processor;
		
		public MyVerifier(BiPredicate<JComponent,ActionEvent> processor) {
			this.processor = processor;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			processor.test(null, e);
		}

		@Override
		public boolean verify(JComponent input) {
			return processor.test(input, null);
		}
	}
}