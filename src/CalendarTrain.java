/**
 * @project:CalendarTrain
 * @Author Liyi
 * @Date 2023/5/22 23:29
 * @Version 1.0
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.swing.*;


public class CalendarTrain extends JFrame implements ActionListener{
    //月份和年份下拉 列表框
    private JComboBox MonthBox = new JComboBox();
    private JComboBox YearBox = new JComboBox();

    //年份月份标签
    private JLabel YearLabel = new JLabel("年份：");
    private JLabel MonthLabel = new JLabel("月份：");

    //确定和今天按钮
    private JButton button_ok = new JButton("查看");
    private JButton button_today = new JButton("今天");

    private JButton memoButton = new JButton("备忘录");

    private JFrame frame;
    private JList<String> eventList;
    private DefaultListModel<String> listModel;
    private Map<String, LocalDateTime> events;
    private JTextField textField;
    private JButton addButton, deleteButton;

    //获取今天的日期、年份和月份
    private Date now_date = new Date();

    private int now_year = now_date.getYear() + 1900;
    private int now_month = now_date.getMonth();
    private boolean todayFlag = false;

    //用一组按钮显示日期，一共7行7列。第一行是星期
    private JButton[] button_day = new JButton[42];
    private final String[] week = {"SUN","MON","TUE","WEN","THR","FRI","SAT"};
    private JButton[] button_week = new JButton[7];

    private String year_int = null;
    private int month_int;

    /*构造函数*/
    public CalendarTrain(){
        super();
        this.setTitle("日历");
        this.init();
        this.setLocation(500, 300);

        this.setResizable(false);
        pack();

    }

    //初始化日历
    private void init() {
        Font font = new Font("Dialog",Font.BOLD,16);
        YearLabel.setFont(font);
        MonthLabel.setFont(font);
        button_ok.setFont(font);
        button_today.setFont(font);
        memoButton.setFont(font);
        //过去20年--未来20年
        for(int i = now_year - 20;i <= now_year + 100;i++){
            YearBox.addItem(i+"");
        }
        YearBox.setSelectedIndex(20);

        for(int i = 1;i <= 13;i++){
            MonthBox.addItem(i+"");
        }
        MonthBox.setSelectedIndex(now_month);

        //放置下拉列表框和控制按钮的面板
        JPanel panel_ym = new JPanel();
        panel_ym.add(YearLabel);
        panel_ym.add(YearBox);
        panel_ym.add(MonthLabel);
        panel_ym.add(MonthBox);
        panel_ym.add(button_ok);
        panel_ym.add(button_today);

        //为按钮添加时间监听器
        button_ok.addActionListener(this);
        button_today.addActionListener(this);
        memoButton.addActionListener(e->memoEvent());


        JPanel panel_day = new JPanel();
        //7*7
        panel_day.setLayout(new GridLayout(7, 7, 3, 3));
        for(int i = 0; i < 7; i++) {
            button_week[i] = new JButton(" ");
            button_week[i].setText(week[i]);
            button_week[i].setForeground(Color.black);
            panel_day.add(button_week[i]);
        }
        button_week[0].setForeground(Color.red);
        button_week[6].setForeground(Color.red);

        for(int i = 0; i < 42;i++){
            button_day[i] = new JButton(" ");
            panel_day.add(button_day[i]);
        }

        this.paintDay();//显示当前日期

        JPanel panel_main = new JPanel();
        panel_main.setLayout(new BorderLayout());
        panel_main.add(panel_day,BorderLayout.SOUTH);
        panel_main.add(panel_ym,BorderLayout.WEST);
        panel_main.add(memoButton,BorderLayout.EAST);
        getContentPane().add(panel_main);

    }



    private void memoEvent(){
        this.memoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if(event.getSource()==memoButton){
                    events=new HashMap<>();
                    frame = new JFrame("备忘录");
                    frame.setSize(500,300);
                    frame.getContentPane().setLayout(new BorderLayout(0, 0));
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    JPanel panel = new JPanel();
                    frame.getContentPane().add(panel, BorderLayout.NORTH);
                    //panel.setBackground(new Color(151, 222, 197));
                    ImageIcon img=new ImageIcon("D:/programming code/java/final_1/src/1.jpg");
                    JLabel ig=new JLabel(img);
                    ig.setSize(frame.getContentPane().getSize());
                    ig.setLocation(0,0);
                    textField = new JTextField();
                    panel.add(textField);
                    textField.setColumns(20);

                    addButton = new JButton("添加");
                    addButton.addActionListener(e -> addEvent());
                    panel.add(addButton);

                    deleteButton = new JButton("删除");
                    deleteButton.addActionListener(e -> deleteEvent());
                    panel.add(deleteButton);

                    listModel = new DefaultListModel<>();
                    eventList = new JList<>(listModel);
                    eventList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

                    JScrollPane scrollPane = new JScrollPane(eventList);
                    frame.getContentPane().add(scrollPane, BorderLayout.CENTER);


                    javax.swing.Timer timer = new javax.swing.Timer(60000, e -> checkEvents());
                    timer.start();
                    /*
                    ImageIcon img=new ImageIcon("D:/programming code/java/final_1/src/1.jpg");
                    JLabel ig=new JLabel(img);
                    ig.setBounds(0,0, img.getIconWidth(), img.getIconHeight());
                    ((JPanel)frame.getContentPane()).setOpaque(true);
                    frame.getLayeredPane().add(ig,new Integer(Integer.MIN_VALUE));
                    */

                    frame.setVisible(true);
                }

            }
        });

    }
    //添加事项
    private void addEvent() {
        String eventText = textField.getText();
        String[] parts = eventText.split(" ", 2);
        if (parts.length == 2) {
            LocalDateTime eventTime = LocalDateTime.parse(parts[0], DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            String eventName = parts[1];
            events.put(eventName, eventTime);
            listModel.addElement(eventText);
            textField.setText("");
        } else {
            JOptionPane.showMessageDialog(frame, "输入格式无效，请按照一下格式输入: yyyy-MM-dd'T'HH:mm eventName");
        }
    }

    //删除事项

    private void deleteEvent() {
        int selectedIndex = eventList.getSelectedIndex();
        if (selectedIndex != -1) {
            String eventName = listModel.getElementAt(selectedIndex);
            events.remove(eventName);
            listModel.remove(selectedIndex);
        }
    }

    //到时提醒
    private void checkEvents() {
        LocalDateTime now = LocalDateTime.now();
        events.entrySet().removeIf(entry -> {
            LocalDateTime eventTime = entry.getValue();
            if (eventTime.isBefore(now)) {
                JOptionPane.showMessageDialog(frame, "备忘录提醒: " + entry.getKey());
                listModel.removeElement(entry.getKey() + " " + entry.getValue().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                return true;
            }
            return false;
        });
    }

    private void paintDay() {
        if(todayFlag){
            year_int = now_year +"";
            month_int = now_month;
        }else{
            year_int = YearBox.getSelectedItem().toString();
            month_int = MonthBox.getSelectedIndex();
        }
        int year_sel = Integer.parseInt(year_int) - 1900;
        Date firstDay = new Date(year_sel, month_int, 1);
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(firstDay);
        int days = 0;
        int day_week = 0;

        if(month_int == 0||month_int == 2||month_int == 4||month_int == 6
                ||month_int == 7||month_int == 9||month_int == 11){
            days = 31;
        }else if(month_int == 3||month_int == 5||month_int == 8||month_int == 10){
            days = 30;
        }else{
            if(cal.isLeapYear(year_sel)){
                days = 29;
            }else{
                days = 28;
            }
        }

        day_week = firstDay.getDay();
        int count = 1;

        for(int i = day_week;i<day_week+days;count++,i++){
            if(i%7 == 0||(i+1)%7 == 0){
                if((i == day_week+now_date.getDate()-1)&& month_int==now_month && (year_sel == now_year-1900)){
                    button_day[i].setForeground(Color.BLUE);
                    button_day[i].setText(count+"");
                }else{
                    button_day[i].setForeground(Color.RED);
                    button_day[i].setText(count+"");
                }
            }else{
                if((i == day_week+now_date.getDate()-1)&& month_int==now_month && (year_sel == now_year-1900)){
                    button_day[i].setForeground(Color.BLUE);
                    button_day[i].setText(count+"");
                }else{
                    button_day[i].setForeground(Color.BLACK);
                    button_day[i].setText(count+"");
                }
            }
        }
        if(day_week == 0){
            for(int i = days;i<42;i++){
                button_day[i].setText("");
            }
        }else{
            for(int i = 0;i<day_week;i++){
                button_day[i].setText("");
            }
            for(int i=day_week+days;i<42;i++){
                button_day[i].setText("");
            }
        }


    }




    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==button_ok){
            todayFlag=false;
            this.paintDay();
        }else if(e.getSource()==button_today){
            todayFlag=true;
            YearBox.setSelectedIndex(20);
            MonthBox.setSelectedIndex(now_month);
            this.paintDay();
        }

    }

    public static void main(String[] args) {
        CalendarTrain ct = new CalendarTrain();
        ct.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ct.setVisible(true);

    }

}
