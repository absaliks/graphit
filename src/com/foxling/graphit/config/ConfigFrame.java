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

package com.foxling.graphit.config;

import java.awt.EventQueue;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.AbstractListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.foxling.graphit.Core;
import com.foxling.graphit.DataType;
import com.foxling.graphit.Field;
import com.foxling.graphit.FieldDelimiter;
import com.foxling.graphit.Item;

import javax.swing.JTabbedPane;
import java.awt.FlowLayout;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import net.miginfocom.swing.MigLayout;
import java.awt.Color;
import javax.swing.BoxLayout;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import java.awt.Dimension;
import javax.swing.JSpinner;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.BevelBorder;
import java.awt.Insets;

import javax.swing.JPopupMenu;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ConfigFrame extends JFrame {
	private static final long serialVersionUID = 3103016344816004897L;
	private JPanel contentPane;
	private JTextField iColumnName;
	private JComboBox<FieldDelimiter> iFieldDelimiter;		
	private JComboBox<DataType> iDataType;
	private JComboBox<String> iFormat;
	private FieldListModel mdlFieldList;
	private JList<Field> iFieldList;
	private JMenuItem miAddField;
	private JMenuItem miRemoveField;
	private JTextField edtFormat;
	private JCheckBox iOptional;
	private JTable tValues;
	
	public ConfigFrame() {
		super("Настройки");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 593, 400);
		
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
		
		JRadioButton iWorkDir = new JRadioButton("Рабочая папка");
		iWorkDir.setSelected(true);
		pnlConfigLocation.add(iWorkDir);
		
		JRadioButton iCurrUser = new JRadioButton("Папка текущего пользователя");
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
	    
	    JComboBox iDefaultFieldDelimiter = new JComboBox();
	    panel.add(iDefaultFieldDelimiter, "cell 1 0,growx");
	    
	    JLabel lblNewLabel = new JLabel("Разделитель строк");
	    panel.add(lblNewLabel, "cell 0 1,alignx trailing");
	    
	    JComboBox iDefaultLineDelimiter = new JComboBox();
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
		
		
		JLabel lblColumnName = new JLabel("Имя");
		JLabel lblColumnDelimiter = new JLabel("Ограничитель столбца");
		JLabel lblDataType = new JLabel("Тип данных");
		JLabel lblOptional = new JLabel("Необязательное");
		JLabel lblFormat = new JLabel("Формат");
		JPanel pnlFormat = new JPanel();
		pnlFormat.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		
		iColumnName = new JTextField();
		iFieldDelimiter = new JComboBox<FieldDelimiter>(new DefaultComboBoxModel<FieldDelimiter>(FieldDelimiter.values()));
		iDataType = new JComboBox<DataType>();
		iDataType.setModel(new DefaultComboBoxModel<DataType>(DataType.values()));
		iOptional = new JCheckBox();
		iFormat = new JComboBox<String>();
		edtFormat = new JTextField();
		pnlFormat.setLayout(new BoxLayout(pnlFormat, BoxLayout.Y_AXIS));
		pnlFormat.add(iFormat);
		pnlFormat.add(edtFormat);
		
		pnlMisc.setLayout(new MigLayout("", "[grow][grow]", "[][][][][][grow]"));
		pnlMisc.add(lblColumnName, "cell 0 0");
		pnlMisc.add(iColumnName, "cell 1 0,growx");
		pnlMisc.add(lblColumnDelimiter, "cell 0 1");
		pnlMisc.add(iFieldDelimiter, "cell 1 1,growx");
		pnlMisc.add(lblDataType, "cell 0 2");
		pnlMisc.add(iDataType, "cell 1 2,growx");
		pnlMisc.add(lblFormat, "cell 0 3");
		pnlMisc.add(pnlFormat, "cell 1 3,grow");
		pnlMisc.add(lblOptional, "cell 0 4");
		pnlMisc.add(iOptional, "cell 1 4,growx");
		
		JPanel pnlValues = new JPanel();
		pnlValues.setBorder(new TitledBorder(null, "\u041D\u0430\u0431\u043E\u0440 \u0437\u043D\u0430\u0447\u0435\u043D\u0438\u0439", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		pnlMisc.add(pnlValues, "cell 0 5 2 1,grow");
		pnlValues.setLayout(new BoxLayout(pnlValues, BoxLayout.Y_AXIS));
		
		JScrollPane spValues = new JScrollPane();
		pnlValues.add(spValues);
		
		tValues = new JTable();
		spValues.setViewportView(tValues);
		
		initControls();
	}
	
	private void initControls(){
		fieldListController();
		fieldEditorController();
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
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
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
	
	private void fieldListController(){
		mdlFieldList = new FieldListModel();
		iFieldList.setModel(mdlFieldList);
		
		Core.getConfigModel().addConfigModelListener((evt) -> {
			mdlFieldList.refresh();
		});
		
		miAddField.addActionListener((ActionEvent arg0) -> {
			Core.getConfigModel().addFieldAfter(iFieldList.getSelectedValue());
		});
		
		miRemoveField.addActionListener((ActionEvent arg0) -> {
			Core.getConfigModel().removeFields(iFieldList.getSelectedValuesList());
		});
	}
	
	private void fieldEditorController(){
		ValueListModel mdlValueList = new ValueListModel();
		tValues.setModel(mdlValueList);
		
		iFieldList.addListSelectionListener((evt) -> {
			Field field = iFieldList.getSelectedValue();
			if (field != null) {
				DataType datatype = field.getDatatype();
				iColumnName.setText(field.getName());
				iFieldDelimiter.setSelectedItem(field.getDelimiter());
				iDataType.setSelectedItem(datatype);
				edtFormat.setText(field.getFormat());
				if (datatype != null)
					iFormat.setSelectedItem(datatype.getFormat(field.getFormat()));
				iOptional.setSelected(field.isOptional());
				
				mdlValueList.setField(field);
			}
		});
	}
	
	private class FieldListModel
	extends AbstractListModel<Field> {
		private static final long serialVersionUID = -6222628541194781163L;

		@Override
		public Field getElementAt(int index) {
			return Core.getConfigModel().getField(index);
		}

		@Override
		public int getSize() {
			return Core.getConfigModel().getFieldSetSize();
		}
		
		public void refresh(){
			int length = getSize();
			if (length > 0)
				this.fireContentsChanged(this, 0, length);
		}
	}
	
	private class ValueListModel
	extends AbstractTableModel {
		private static final long serialVersionUID = 3742047021848215242L;
		private final String[] COLS = { "Значение", "Описание" };
		private final Class[] COL_CLASS = {Object.class, String.class};
		private List<Item> valueList;
		
		@Override
		public int getColumnCount() {
			return COLS.length;
		}
		
		@Override
		public String getColumnName(int col) {
			return COLS[col];
		}
		
		@Override
		public Class getColumnClass(int col) {
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
				default:
					return null;
				}
			} else
				return null;
		}
		
		public void setField(Field field){
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
}
