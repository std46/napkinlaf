/*
 * @(#)SwingSet2.java	1.50 04/07/26
 *
 * Copyright (c) 2004 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * -Redistribution of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 *
 * -Redistribution in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */

/*
 * @(#)SwingSet2.java	1.50 04/07/26
 */

import net.sourceforge.napkinlaf.NapkinTheme;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.plaf.metal.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * A demo that shows all of the Swing components.
 *
 * @author Jeff Dinkins
 * @version 1.50 07/26/04
 */
public class SwingSet2 extends JPanel {
    String[] demos = {
            "ButtonDemo",
            "ColorChooserDemo",
            "ComboBoxDemo",
            "FileChooserDemo",
            "HtmlDemo",
            "ListDemo",
            "OptionPaneDemo",
            "ProgressBarDemo",
            "ScrollPaneDemo",
            "SliderDemo",
            "SplitPaneDemo",
            "TabbedPaneDemo",
            "TableDemo",
            "ToolTipDemo",
            "TreeDemo"
    };

    private static final String DEFAULT_THEME_PREFIX = "default:";

    void loadDemos() {
        for (int i = 0; i < demos.length;) {
            if (isApplet() && demos[i].equals("FileChooserDemo")) {
                // don't load the file chooser demo if we are
                // an applet
            } else {
                loadDemo(demos[i]);
            }
            i++;
        }
    }

    private static String metal =
            "javax.swing.plaf.metal.MetalLookAndFeel";

    // The current Look & Feel
    private static String currentLookAndFeel = metal;
    private static String themesMenuLookAndFeel = metal;

    // List of demos
    private ArrayList<DemoModule> demosList = new ArrayList<DemoModule>();

    // The preferred size of the demo
    private static final int PREFERRED_WIDTH = 720;
    private static final int PREFERRED_HEIGHT = 640;

    // Box spacers
    private Dimension HGAP = new Dimension(1, 5);
    private Dimension VGAP = new Dimension(5, 1);

    // Resource bundle for internationalized and accessible text
    private ResourceBundle bundle = null;

    // A place to hold on to the visible demo
    private DemoModule currentDemo = null;
    private JPanel demoPanel = null;

    // About Box
    private JDialog aboutBox = null;

    // Status Bar
    private JTextField statusField = null;

    // Tool Bar
    private ToggleButtonToolBar toolbar = null;
    private ButtonGroup toolbarGroup = new ButtonGroup();

    // Menus
    private JMenuBar menuBar = null;
    private JMenu lafMenu = null;
    private JMenu themesMenu = null;
    private JMenu audioMenu = null;
    private JMenu optionsMenu = null;
    private ButtonGroup lafMenuGroup = new ButtonGroup();
    private ButtonGroup themesMenuGroup = new ButtonGroup();
    private ButtonGroup audioMenuGroup = new ButtonGroup();

    private Map<String, String> themesFor = new HashMap<String, String>();
    private Map<String, String> namesFor = new HashMap<String, String>();

    // Popup menu
    private JPopupMenu popupMenu = null;
    private ButtonGroup popupMenuGroup = new ButtonGroup();

    // Used only if swingset is an application
    private JFrame frame = null;
    private JWindow splashScreen = null;

    // Used only if swingset is an applet
    private SwingSet2Applet applet = null;

    // To debug or not to debug, that is the question
    private boolean DEBUG = true;
    private int debugCounter = 0;

    // The tab pane that holds the demo
    private JTabbedPane tabbedPane = null;

    private JEditorPane demoSrcPane = null;

    private JLabel splashLabel = null;

    // contentPane cache, saved from the applet or application frame
    Container contentPane = null;

    private String initLaf = null;

    // number of swingsets - for multiscreen
    // keep track of the number of SwingSets created - we only want to exit
    // the program when the last one has been closed.
    private static int numSSs = 0;
    private static Vector<SwingSet2> swingSets = new Vector<SwingSet2>();

    private boolean dragEnabled = false;

    public SwingSet2(SwingSet2Applet applet) {
        this(applet, null);
    }

    /** SwingSet2 Constructor */
    public SwingSet2(SwingSet2Applet applet, GraphicsConfiguration gc) {

        // Note that the applet may null if this is started as an application
        this.applet = applet;

        // Create Frame here for app-mode so the splash screen can get the
        // GraphicsConfiguration from it in createSplashScreen()
        if (!isApplet()) {
            frame = createFrame(gc);
        }

        // setLayout(new BorderLayout());
        setLayout(new BorderLayout());

        // set the preferred size of the demo
        setPreferredSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));

        // Create and throw the splash screen up. Since this will
        // physically throw bits on the screen, we need to do this
        // on the GUI thread using invokeLater.
        createSplashScreen();

        // do the following on the gui thread
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                showSplashScreen();
            }
        });

        initializeDemo();
        preloadFirstDemo();

        // Show the demo and take down the splash screen. Note that
        // we again must do this on the GUI thread using invokeLater.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                showSwingSet2();
                hideSplash();
            }
        });

        // Start loading the rest of the demo in the background
        DemoLoadThread demoLoader = new DemoLoadThread(this);
        demoLoader.start();
    }

    /** SwingSet2 Main. Called only if we're an application, not an applet. */
    public static void main(String[] args) {
        // Create SwingSet on the default monitor
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        SwingSet2 swingset = new SwingSet2(null, GraphicsEnvironment.
                getLocalGraphicsEnvironment().
                getDefaultScreenDevice().
                getDefaultConfiguration());
    }

    // *******************************************************
    // *************** Demo Loading Methods ******************
    // *******************************************************

    public void initializeDemo() {
        initLaf = getString("LafMenu.laf_default").trim();
        setLookAndFeel(getLafClass(initLaf));

        JPanel top = new JPanel();
        top.setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);

        menuBar = createMenus();

        if (isApplet()) {
            applet.setJMenuBar(menuBar);
        } else {
            frame.setJMenuBar(menuBar);
        }

        // creates popup menu accessible via keyboard
        popupMenu = createPopupMenu();
        createFormalControls();

        ToolBarPanel toolbarPanel = new ToolBarPanel();
        toolbarPanel.setLayout(new BorderLayout());
        toolbar = new ToggleButtonToolBar();
        toolbarPanel.add(toolbar, BorderLayout.CENTER);
        top.add(toolbarPanel, BorderLayout.SOUTH);
        toolbarPanel.addContainerListener(toolbarPanel);

        tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);
        tabbedPane.getModel().addChangeListener(new TabListener());

        statusField = new JTextField("");
        statusField.setEditable(false);
        add(statusField, BorderLayout.SOUTH);

        demoPanel = new JPanel();
        demoPanel.setLayout(new BorderLayout());
        demoPanel.setBorder(new EtchedBorder());
        tabbedPane.addTab("Hi There!", demoPanel);

        // Add html src code viewer
        demoSrcPane = new JEditorPane("text/html",
                getString("SourceCode.loading"));
        demoSrcPane.setEditable(false);

        JScrollPane scroller = new JScrollPane();
        scroller.getViewport().add(demoSrcPane);

        tabbedPane.addTab(
                getString("TabbedPane.src_label"),
                null,
                scroller,
                getString("TabbedPane.src_tooltip")
        );
    }

    private void createFormalControls() {
        InputMap map = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,
                InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK);
        map.put(key, "invokeFormality");
        getActionMap().put("invokeFormality", new FormalityChangeToggleAction(this));
    }

    DemoModule currentTabDemo = null;

    class TabListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            SingleSelectionModel model = (SingleSelectionModel) e.getSource();
            boolean srcSelected = model.getSelectedIndex() == 1;
            if (currentTabDemo != currentDemo && demoSrcPane != null &&
                    srcSelected) {
                demoSrcPane.setText(getString("SourceCode.loading"));
                repaint();
            }
            if (currentTabDemo != currentDemo && srcSelected) {
                currentTabDemo = currentDemo;
                setSourceCode(currentDemo);
            }
        }
    }

    /** Create menus */
    public JMenuBar createMenus() {
        JMenuItem mi;
        // ***** create the menubar ****
        JMenuBar menuBar = new JMenuBar();
        menuBar.getAccessibleContext().setAccessibleName(
                getString("MenuBar.accessible_description"));

        // ***** create File menu
        JMenu fileMenu = (JMenu) menuBar
                .add(new JMenu(getString("FileMenu.file_label")));
        fileMenu.setMnemonic(getMnemonic("FileMenu.file_mnemonic"));
        fileMenu.getAccessibleContext()
                .setAccessibleDescription(
                        getString("FileMenu.accessible_description"));

        createMenuItem(fileMenu, "FileMenu.about_label",
                "FileMenu.about_mnemonic",
                "FileMenu.about_accessible_description", new AboutAction(this));

        fileMenu.addSeparator();

        createMenuItem(fileMenu, "FileMenu.open_label",
                "FileMenu.open_mnemonic",
                "FileMenu.open_accessible_description", null);

        createMenuItem(fileMenu, "FileMenu.save_label",
                "FileMenu.save_mnemonic",
                "FileMenu.save_accessible_description", null);

        createMenuItem(fileMenu, "FileMenu.save_as_label",
                "FileMenu.save_as_mnemonic",
                "FileMenu.save_as_accessible_description", null);

        if (!isApplet()) {
            fileMenu.addSeparator();

            createMenuItem(fileMenu, "FileMenu.exit_label",
                    "FileMenu.exit_mnemonic",
                    "FileMenu.exit_accessible_description", new ExitAction(this)
            );
        }

        // Create these menu items for the first SwingSet only.
        if (numSSs == 0) {
            // ***** create laf switcher menu
            lafMenu = (JMenu) menuBar
                    .add(new JMenu(getString("LafMenu.laf_label")));
            lafMenu.setMnemonic(getMnemonic("LafMenu.laf_mnemonic"));
            lafMenu.getAccessibleContext().setAccessibleDescription(
                    getString("LafMenu.laf_accessible_description"));

            String[] lafs = split(getString("LafMenu.laf_list"));
            for (String laf : lafs) {
                String lafClass = getLafClass(laf);
                String pref = "LafMenu." + laf + "_";
                mi = createLafMenuItem(lafMenu, pref + "label",
                        pref + "mnemonic", pref + "accessible_description",
                        lafClass);
                if (laf.equals(initLaf)) {
                    mi.setSelected(true);
                }
                if (laf.equals("java"))
                    metal = lafClass;
                String themes = getStringIfPresent(pref + "themes");
                if (themes != null) {
                    themesFor.put(lafClass, themes);
                    namesFor.put(lafClass, laf);
                }
            }
            SwingUtilities.updateComponentTreeUI(lafMenu);
            SwingUtilities.updateComponentTreeUI(fileMenu);

            // ***** create themes menu
            themesMenu = new JMenu(getString("ThemesMenu.themes_label"));
            menuBar.add(themesMenu);
            themesMenu.setMnemonic(getMnemonic("ThemesMenu.themes_mnemonic"));
            themesMenu.getAccessibleContext().setAccessibleDescription(
                    getString("ThemesMenu.themes_accessible_description"));

            populateThemesMenu();

            // ***** create the audio submenu under the theme menu
            audioMenu = (JMenu) this.themesMenu
                    .add(new JMenu(getString("AudioMenu.audio_label")));
            audioMenu.setMnemonic(getMnemonic("AudioMenu.audio_mnemonic"));
            audioMenu.getAccessibleContext().setAccessibleDescription(
                    getString("AudioMenu.audio_accessible_description"));

            createAudioMenuItem(audioMenu, "AudioMenu.on_label",
                    "AudioMenu.on_mnemonic",
                    "AudioMenu.on_accessible_description",
                    new OnAudioAction(this));

            mi = createAudioMenuItem(audioMenu, "AudioMenu.default_label",
                    "AudioMenu.default_mnemonic",
                    "AudioMenu.default_accessible_description",
                    new DefaultAudioAction(this));
            mi.setSelected(true); // This is the default feedback setting

            createAudioMenuItem(audioMenu, "AudioMenu.off_label",
                    "AudioMenu.off_mnemonic",
                    "AudioMenu.off_accessible_description",
                    new OffAudioAction(this));

            // ***** create the font submenu under the theme menu
            JMenu fontMenu = (JMenu) this.themesMenu
                    .add(new JMenu(getString("FontMenu.fonts_label")));
            fontMenu.setMnemonic(getMnemonic("FontMenu.fonts_mnemonic"));
            fontMenu.getAccessibleContext().setAccessibleDescription(
                    getString("FontMenu.fonts_accessible_description"));
            ButtonGroup fontButtonGroup = new ButtonGroup();
            mi = createButtonGroupMenuItem(fontMenu, "FontMenu.plain_label",
                    "FontMenu.plain_mnemonic",
                    "FontMenu.plain_accessible_description",
                    new ChangeFontAction(this, true), fontButtonGroup);
            mi.setSelected(true);
            mi = createButtonGroupMenuItem(fontMenu, "FontMenu.bold_label",
                    "FontMenu.bold_mnemonic",
                    "FontMenu.bold_accessible_description",
                    new ChangeFontAction(this, false), fontButtonGroup);

            // ***** create the options menu
            optionsMenu = (JMenu) menuBar.add(
                    new JMenu(getString("OptionsMenu.options_label")));
            optionsMenu
                    .setMnemonic(getMnemonic("OptionsMenu.options_mnemonic"));
            optionsMenu.getAccessibleContext().setAccessibleDescription(
                    getString("OptionsMenu.options_accessible_description"));

            // ***** create tool tip submenu item.
            mi = createCheckBoxMenuItem(optionsMenu,
                    "OptionsMenu.tooltip_label",
                    "OptionsMenu.tooltip_mnemonic",
                    "OptionsMenu.tooltip_accessible_description",
                    new ToolTipAction());
            mi.setSelected(true);

            // ***** create drag support submenu item.
            createCheckBoxMenuItem(optionsMenu, "OptionsMenu.dragEnabled_label",
                    "OptionsMenu.dragEnabled_mnemonic",
                    "OptionsMenu.dragEnabled_accessible_description",
                    new DragSupportAction());
        }

        // ***** create the multiscreen menu, if we have multiple screens
        if (!isApplet()) {
            GraphicsDevice[] screens = GraphicsEnvironment.
                    getLocalGraphicsEnvironment().
                    getScreenDevices();
            if (screens.length > 1) {

                JMenu multiScreenMenu = (JMenu) menuBar.add(new JMenu(
                        getString("MultiMenu.multi_label")));

                multiScreenMenu
                        .setMnemonic(getMnemonic("MultiMenu.multi_mnemonic"));
                multiScreenMenu.getAccessibleContext().setAccessibleDescription(
                        getString("MultiMenu.multi_accessible_description"));

                createMultiscreenMenuItem(multiScreenMenu,
                        MultiScreenAction.ALL_SCREENS);
                for (int i = 0; i < screens.length; i++) {
                    createMultiscreenMenuItem(multiScreenMenu, i);
                }
            }
        }

        return menuBar;
    }

    private void populateThemesMenu() {
        if (themesMenu == null)
            return;

        if (themesMenuLookAndFeel == currentLookAndFeel)
            return;
        themesMenuLookAndFeel = currentLookAndFeel;

        String themesList = (String) themesFor.get(currentLookAndFeel);
        if (themesList == null) {
            themesMenu.setEnabled(false);
            return;
        }

        themesMenu.removeAll();
        themesMenuGroup = new ButtonGroup(); // create a new empty one

        String laf = (String) namesFor.get(currentLookAndFeel);
        StringTokenizer themes = new StringTokenizer(themesList, ", \t\n\r\f");
        while (themes.hasMoreTokens()) {
            String theme = themes.nextToken();
            boolean isDefault = theme.startsWith(DEFAULT_THEME_PREFIX);
            if (isDefault)
                theme = theme.substring(DEFAULT_THEME_PREFIX.length());
            String prefix = "ThemesMenu." + laf + "." + theme + "_";
            Object themeObj;
            try {
                String className = getStringIfPresent(prefix + "class");
                if (className != null) {
                    Class themeClass = Class.forName(className);
                    themeObj = themeClass.newInstance();
                } else {
                    themeObj = getStringIfPresent(prefix + "name");
                }
            } catch (Exception e) {
                themeObj = null;
            }
            JMenuItem mi = createThemesMenuItem(themesMenu,
                    prefix + "label", prefix + "mnemonic",
                    prefix + "accessible_description",
                    themeObj);
            if (isDefault)
                mi.setSelected(true);
        }

        themesMenu.setEnabled(true);
    }

    private String getLafClass(String laf) {
        String osName = System.getProperty("os.name");
        if (laf.equals("mac") && osName.indexOf("Mac") >= 0)
            return UIManager.getSystemLookAndFeelClassName().intern();

        /**
         * The return below works for all systems I know of *except* the Mac,
         * which has different names for the LAF class on different platforms.
         * So we do the check above because that's what the Mac's own version of
         * the SwingSet does to work around this.  Looks pretty weird, and if I
         * knew a more general way to do this, I'd do that instead.
         */
        return getString("LafMenu." + laf + "_class").intern();
    }

    private String[] split(String string) {
        StringTokenizer toks = new StringTokenizer(string, "\t\n\r ,");
        String[] strs = new String[toks.countTokens()];
        for (int i = 0; toks.hasMoreTokens(); i++)
            strs[i] = toks.nextToken().intern();
        return strs;
    }

    /** Create a checkbox menu menu item */
    private JMenuItem createCheckBoxMenuItem(JMenu menu, String label,
            String mnemonic,
            String accessibleDescription,
            Action action) {
        JCheckBoxMenuItem mi = (JCheckBoxMenuItem) menu.add(
                new JCheckBoxMenuItem(getString(label)));
        mi.setMnemonic(getMnemonic(mnemonic));
        mi.getAccessibleContext().setAccessibleDescription(getString(
                accessibleDescription));
        mi.addActionListener(action);
        return mi;
    }

    private JMenuItem createButtonGroupMenuItem(JMenu menu, String label,
            String mnemonic,
            String accessibleDescription,
            Action action,
            ButtonGroup buttonGroup) {
        JRadioButtonMenuItem mi = (JRadioButtonMenuItem) menu.add(
                new JRadioButtonMenuItem(getString(label)));
        buttonGroup.add(mi);
        mi.setMnemonic(getMnemonic(mnemonic));
        mi.getAccessibleContext().setAccessibleDescription(getString(
                accessibleDescription));
        mi.addActionListener(action);
        return mi;
    }

    /** Create the theme's audio submenu */
    public JMenuItem createAudioMenuItem(JMenu menu, String label,
            String mnemonic,
            String accessibleDescription,
            Action action) {
        JRadioButtonMenuItem mi = (JRadioButtonMenuItem) menu
                .add(new JRadioButtonMenuItem(getString(label)));
        audioMenuGroup.add(mi);
        mi.setMnemonic(getMnemonic(mnemonic));
        mi.getAccessibleContext()
                .setAccessibleDescription(getString(accessibleDescription));
        mi.addActionListener(action);

        return mi;
    }

    /** Creates a generic menu item */
    public JMenuItem createMenuItem(JMenu menu, String label, String mnemonic,
            String accessibleDescription, Action action) {
        JMenuItem mi = (JMenuItem) menu.add(new JMenuItem(getString(label)));
        mi.setMnemonic(getMnemonic(mnemonic));
        mi.getAccessibleContext()
                .setAccessibleDescription(getString(accessibleDescription));
        mi.addActionListener(action);
        if (action == null) {
            mi.setEnabled(false);
        }
        return mi;
    }

    /** Creates a JRadioButtonMenuItem for the Themes menu */
    public JMenuItem createThemesMenuItem(JMenu menu, String label,
            String mnemonic,
            String accessibleDescription, Object theme) {
        JRadioButtonMenuItem mi = (JRadioButtonMenuItem) menu.add(
                new JRadioButtonMenuItem(getString(label)));
        themesMenuGroup.add(mi);
        mi.setMnemonic(getMnemonic(mnemonic));
        mi.getAccessibleContext().setAccessibleDescription(
                getString(accessibleDescription));
        if (theme instanceof MetalTheme)
            mi.addActionListener(
                    new ChangeThemeAction(this, (MetalTheme) theme));
        else if (theme instanceof String)
            mi.addActionListener(new ChangeThemeAction(this, (String) theme));
        else
            mi.setEnabled(false);

        return mi;
    }

    /** Creates a JRadioButtonMenuItem for the Look and Feel menu */
    public JMenuItem createLafMenuItem(JMenu menu, String label,
            String mnemonic,
            String accessibleDescription, String laf) {
        JMenuItem mi = (JRadioButtonMenuItem) menu
                .add(new JRadioButtonMenuItem(getString(label)));
        lafMenuGroup.add(mi);
        mi.setMnemonic(getMnemonic(mnemonic));
        mi.getAccessibleContext()
                .setAccessibleDescription(getString(accessibleDescription));
        mi.addActionListener(new ChangeLookAndFeelAction(this, laf));

        mi.setEnabled(isAvailableLookAndFeel(laf));

        return mi;
    }

    /** Creates a multi-screen menu item */
    public JMenuItem createMultiscreenMenuItem(JMenu menu, int screen) {
        JMenuItem mi = null;
        if (screen == MultiScreenAction.ALL_SCREENS) {
            mi = (JMenuItem) menu
                    .add(new JMenuItem(getString("MultiMenu.all_label")));
            mi.setMnemonic(getMnemonic("MultiMenu.all_mnemonic"));
            mi.getAccessibleContext().setAccessibleDescription(getString(
                    "MultiMenu.all_accessible_description"));
        } else {
            mi = (JMenuItem) menu
                    .add(new JMenuItem(getString("MultiMenu.single_label") +
                            " " +
                            screen));
            mi.setMnemonic(KeyEvent.VK_0 + screen);
            mi.getAccessibleContext().setAccessibleDescription(getString(
                    "MultiMenu.single_accessible_description") + " " + screen);
        }
        mi.addActionListener(new MultiScreenAction(this, screen));
        return mi;
    }

    public JPopupMenu createPopupMenu() {
        JPopupMenu popup = new JPopupMenu("JPopupMenu demo");

        String[] lafs = split(getString("LafMenu.laf_list"));
        for (int i = 0; i < lafs.length; i++) {
            String laf = lafs[i];
            String prefix = "LafMenu." + laf + "_";
            createPopupMenuItem(popup, prefix + "label", prefix + "mnemonic",
                    prefix + "accessible_description", getLafClass(laf));
        }

        // register key binding to activate popup menu
        InputMap map = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        map.put(
                KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK),
                "postMenuAction");
        getActionMap().put("postMenuAction",
                new ActivatePopupMenuAction(this, popup));

        return popup;
    }

    /** Creates a JMenuItem for the Look and Feel popup menu */
    public JMenuItem createPopupMenuItem(JPopupMenu menu, String label,
            String mnemonic,
            String accessibleDescription, String laf) {
        JMenuItem mi = menu.add(new JMenuItem(getString(label)));
        popupMenuGroup.add(mi);
        mi.setMnemonic(getMnemonic(mnemonic));
        mi.getAccessibleContext()
                .setAccessibleDescription(getString(accessibleDescription));
        mi.addActionListener(new ChangeLookAndFeelAction(this, laf));
        mi.setEnabled(isAvailableLookAndFeel(laf));

        return mi;
    }

    /**
     * Load the first demo. This is done separately from the remaining demos so
     * that we can get SwingSet2 up and available to the user quickly.
     */
    public void preloadFirstDemo() {
        DemoModule demo = addDemo(new InternalFrameDemo(this));
        setDemo(demo);
    }

    /** Add a demo to the toolbar */
    public DemoModule addDemo(DemoModule demo) {
        demosList.add(demo);
        if (dragEnabled) {
            demo.updateDragEnabled(true);
        }
        // do the following on the gui thread
        SwingUtilities.invokeLater(new SwingSetRunnable(this, demo) {
            public void run() {
                SwitchToDemoAction action = new SwitchToDemoAction(swingset,
                        (DemoModule) obj);
                JToggleButton tb = swingset.getToolBar()
                        .addToggleButton(action);
                swingset.getToolBarGroup().add(tb);
                if (swingset.getToolBarGroup().getSelection() == null) {
                    tb.setSelected(true);
                }
                tb.setText(null);
                tb.setToolTipText(((DemoModule) obj).getToolTip());

                if (demos[demos.length - 1].equals(obj.getClass().getName())) {
                    setStatus(getString("Status.popupMenuAccessible"));
                }
            }
        });
        return demo;
    }

    /** Sets the current demo */
    public void setDemo(DemoModule demo) {
        currentDemo = demo;

        // Ensure panel's UI is current before making visible
        JComponent currentDemoPanel = demo.getDemoPanel();
        SwingUtilities.updateComponentTreeUI(currentDemoPanel);

        demoPanel.removeAll();
        demoPanel.add(currentDemoPanel, BorderLayout.CENTER);

        tabbedPane.setSelectedIndex(0);
        tabbedPane.setTitleAt(0, demo.getName());
        tabbedPane.setToolTipTextAt(0, demo.getToolTip());
    }

    /**
     * Bring up the SwingSet2 demo by showing the frame (only applicable if
     * coming up as an application, not an applet);
     */
    public void showSwingSet2() {
        if (!isApplet() && getFrame() != null) {
            // put swingset in a frame and show it
            JFrame f = getFrame();
            f.setTitle(getString("Frame.title"));
            f.getContentPane().add(this, BorderLayout.CENTER);
            f.pack();

            Rectangle screenRect = f.getGraphicsConfiguration().getBounds();
            Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(
                    f.getGraphicsConfiguration());

            // Make sure we don't place the demo off the screen.
            int centerWidth = screenRect.width < f.getSize().width ?
                    screenRect.x :
                    screenRect.x + screenRect.width / 2 - f.getSize().width / 2;
            int centerHeight = screenRect.height < f.getSize().height ?
                    screenRect.y :
                    screenRect.y + screenRect.height / 2 -
                            f.getSize().height / 2;

            centerHeight = centerHeight < screenInsets.top ?
                    screenInsets.top : centerHeight;

            f.setLocation(centerWidth, centerHeight);
            f.setVisible(true);
            numSSs++;
            swingSets.add(this);
        }
    }

    /** Show the spash screen while the rest of the demo loads */
    public void createSplashScreen() {
        splashLabel = new JLabel(createImageIcon("Splash.jpg",
                "Splash.accessible_description"));

        if (!isApplet()) {
            splashScreen = new JWindow(getFrame());
            splashScreen.getContentPane().add(splashLabel);
            splashScreen.pack();
            Rectangle screenRect = getFrame().getGraphicsConfiguration()
                    .getBounds();
            splashScreen.setLocation(
                    screenRect.x + screenRect.width / 2 -
                            splashScreen.getSize().width / 2,
                    screenRect.y + screenRect.height / 2 -
                            splashScreen.getSize().height / 2);
        }
    }

    public void showSplashScreen() {
        if (!isApplet()) {
            splashScreen.setVisible(true);
        } else {
            add(splashLabel, BorderLayout.CENTER);
            validate();
            repaint();
        }
    }

    /** pop down the spash screen */
    public void hideSplash() {
        if (!isApplet()) {
            splashScreen.setVisible(false);
            splashScreen = null;
            splashLabel = null;
        }
    }

    // *******************************************************
    // ****************** Utility Methods ********************
    // *******************************************************

    /** Loads a demo from a classname */
    void loadDemo(String classname) {
        setStatus(getString("Status.loading") + getString(classname + ".name"));
        DemoModule demo = null;
        try {
            Class demoClass = Class.forName(classname);
            Constructor demoConstructor = demoClass
                    .getConstructor(new Class[]{SwingSet2.class});
            demo = (DemoModule) demoConstructor
                    .newInstance(new Object[]{this});
            addDemo(demo);
        } catch (Exception e) {
            System.out.println("Error occurred loading demo: " + classname);
            e.printStackTrace();
        }
    }

    /**
     * A utility function that layers on top of the LookAndFeel's
     * isSupportedLookAndFeel() method. Returns true if the LookAndFeel is
     * supported. Returns false if the LookAndFeel is not supported and/or if
     * there is any kind of error checking if the LookAndFeel is supported.
     * <p/>
     * The L&F menu will use this method to detemine whether the various L&F
     * options should be active or inactive.
     */
    protected boolean isAvailableLookAndFeel(String laf) {
        try {
            Class lnfClass = Class.forName(laf);
            LookAndFeel newLAF = (LookAndFeel) (lnfClass.newInstance());
            return newLAF.isSupportedLookAndFeel();
        } catch (Exception e) { // If ANYTHING weird happens, return false
            return false;
        }
    }

    /** Determines if this is an applet or application */
    public boolean isApplet() {
        return (applet != null);
    }

    /** Returns the applet instance */
    public SwingSet2Applet getApplet() {
        return applet;
    }

    /** Returns the frame instance */
    public JFrame getFrame() {
        return frame;
    }

    /** Returns the menubar */
    public JMenuBar getMenuBar() {
        return menuBar;
    }

    /** Returns the toolbar */
    public ToggleButtonToolBar getToolBar() {
        return toolbar;
    }

    /** Returns the toolbar button group */
    public ButtonGroup getToolBarGroup() {
        return toolbarGroup;
    }

    /** Returns the content pane wether we're in an applet or application */
    public Container getContentPane() {
        if (contentPane == null) {
            if (getFrame() != null) {
                contentPane = getFrame().getContentPane();
            } else if (getApplet() != null) {
                contentPane = getApplet().getContentPane();
            }
        }
        return contentPane;
    }

    /**
     * Create a frame for SwingSet2 to reside in if brought up as an
     * application.
     */
    public static JFrame createFrame(GraphicsConfiguration gc) {
        JFrame frame = new JFrame(gc);
        if (numSSs == 0) {
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        } else {
            WindowListener l = new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    numSSs--;
                    swingSets.remove(this);
                }
            };
            frame.addWindowListener(l);
        }
        return frame;
    }

    /** Set the status */
    public void setStatus(String s) {
        // do the following on the gui thread
        SwingUtilities.invokeLater(new SwingSetRunnable(this, s) {
            public void run() {
                swingset.statusField.setText((String) obj);
            }
        });
    }

    /** This method returns a string from the demo's resource bundle. */
    public String getString(String key) {
        String value = null;
        try {
            value = getResourceBundle().getString(key);
        } catch (MissingResourceException e) {
            System.out
                    .println(
                            "java.util.MissingResourceException: Couldn't find value for: " +
                                    key);
        }
        if (value == null) {
            value = "Could not find resource: " + key + "  ";
        }
        return value;
    }

    public String getStringIfPresent(String key) {
        try {
            return getResourceBundle().getString(key);
        } catch (MissingResourceException e) {
            return null;
        }
    }

    void setDragEnabled(boolean dragEnabled) {
        if (dragEnabled == this.dragEnabled) {
            return;
        }

        this.dragEnabled = dragEnabled;

        for (DemoModule dm : demosList) {
            dm.updateDragEnabled(dragEnabled);
        }

        demoSrcPane.setDragEnabled(dragEnabled);
    }

    boolean isDragEnabled() {
        return dragEnabled;
    }

    /**
     * Returns the resource bundle associated with this demo. Used to get
     * accessable and internationalized strings.
     */
    public ResourceBundle getResourceBundle() {
        if (bundle == null) {
            bundle = ResourceBundle.getBundle("resources.swingset");
        }
        return bundle;
    }

    /**
     * Returns a mnemonic from the resource bundle. Typically used as keyboard
     * shortcuts in menu items.
     */
    public char getMnemonic(String key) {
        return (getString(key)).charAt(0);
    }

    /** Creates an icon from an image contained in the "images" directory. */
    public ImageIcon createImageIcon(String filename, String description) {
        String path = "/resources/images/" + filename;
        return new ImageIcon(getClass().getResource(path));
    }

    /** If DEBUG is defined, prints debug information out to std ouput. */
    public void debug(String s) {
        if (DEBUG) {
            System.out.println((debugCounter++) + ": " + s);
        }
    }

    /** Stores the current L&F, and calls updateLookAndFeel, below */
    public void setLookAndFeel(String laf) {
        if (currentLookAndFeel == null || !currentLookAndFeel.equals(laf)) {
            currentLookAndFeel = laf;
            updateLookAndFeel();
        }
    }

    private void updateThisSwingSet() {
        if (isApplet()) {
            SwingUtilities.updateComponentTreeUI(getApplet());
        } else {
            JFrame frame = getFrame();
            if (frame == null) {
                SwingUtilities.updateComponentTreeUI(this);
            } else {
                if (getParent() != frame) // FIX
                    SwingUtilities.updateComponentTreeUI(this); // FIX
                SwingUtilities.updateComponentTreeUI(frame);
                frame.dispose();
                if (UIManager.getLookAndFeel().getSupportsWindowDecorations()) {
                    JFrame.setDefaultLookAndFeelDecorated(true);
                    JDialog.setDefaultLookAndFeelDecorated(true);
                    frame.setUndecorated(true);
                    frame.getRootPane().setWindowDecorationStyle(
                            JRootPane.FRAME);
                } else {
                    JFrame.setDefaultLookAndFeelDecorated(false);
                    JDialog.setDefaultLookAndFeelDecorated(false);
                    frame.setUndecorated(false);
                }
                frame.setVisible(true);
            }
        }

        if (popupMenu != null)
            SwingUtilities.updateComponentTreeUI(popupMenu);
        if (aboutBox != null) {
            SwingUtilities.updateComponentTreeUI(aboutBox);
        }
    }

    /** Sets the current L&F on each demo module */
    public void updateLookAndFeel() {
        try {
            UIManager.setLookAndFeel(currentLookAndFeel);
            for (Iterator itr = swingSets.iterator(); itr.hasNext();) {
                SwingSet2 ss = (SwingSet2) itr.next();
                SwingUtilities.updateComponentTreeUI(ss);
            }
            // update LAF for the toplevel frame, too
            if (!isApplet()) {
                updateThisSwingSet();
            } else {
                for (SwingSet2 ss : swingSets) {
                    ss.updateThisSwingSet();
                }
            }
        } catch (Exception ex) {
            System.out.println("Failed loading L&F: " + currentLookAndFeel);
            System.out.println(ex);
        }
    }

    /**
     * Loads and puts the source code text into JEditorPane in the "Source Code"
     * tab
     */
    public void setSourceCode(DemoModule demo) {
        // do the following on the gui thread
        SwingUtilities.invokeLater(new SwingSetRunnable(this, demo) {
            public void run() {
                swingset.demoSrcPane
                        .setText(((DemoModule) obj).getSourceCode());
                swingset.demoSrcPane.setCaretPosition(0);
            }
        });
    }

    // *******************************************************
    // **************   ToggleButtonToolbar  *****************
    // *******************************************************
    static Insets zeroInsets = new Insets(1, 1, 1, 1);

    protected class ToggleButtonToolBar extends JToolBar {
        public ToggleButtonToolBar() {
            super();
        }

        JToggleButton addToggleButton(Action a) {
            JToggleButton tb = new JToggleButton(
                    (String) a.getValue(Action.NAME),
                    (Icon) a.getValue(Action.SMALL_ICON)
            );
            tb.setMargin(zeroInsets);
            tb.setText(null);
            tb.setEnabled(a.isEnabled());
            tb.setToolTipText((String) a.getValue(Action.SHORT_DESCRIPTION));
            tb.setAction(a);
            add(tb);
            return tb;
        }
    }

    // *******************************************************
    // *********  ToolBar Panel / Docking Listener ***********
    // *******************************************************
    class ToolBarPanel extends JPanel implements ContainerListener {
        public boolean contains(int x, int y) {
            Component c = getParent();
            if (c != null) {
                Rectangle r = c.getBounds();
                return (x >= 0) && (x < r.width) && (y >= 0) && (y < r.height);
            } else {
                return super.contains(x, y);
            }
        }

        public void componentAdded(ContainerEvent e) {
            Container c = e.getContainer().getParent();
            if (c != null) {
                c.getParent().validate();
                c.getParent().repaint();
            }
        }

        public void componentRemoved(ContainerEvent e) {
            Container c = e.getContainer().getParent();
            if (c != null) {
                c.getParent().validate();
                c.getParent().repaint();
            }
        }
    }

    // *******************************************************
    // ******************   Runnables  ***********************
    // *******************************************************

    /**
     * Generic SwingSet2 runnable. This is intended to run on the AWT gui event
     * thread so as not to muck things up by doing gui work off the gui thread.
     * Accepts a SwingSet2 and an Object as arguments, which gives subtypes of
     * this class the two "must haves" needed in most runnables for this demo.
     */
    class SwingSetRunnable implements Runnable {
        protected SwingSet2 swingset;
        protected Object obj;

        public SwingSetRunnable(SwingSet2 swingset, Object obj) {
            this.swingset = swingset;
            this.obj = obj;
        }

        public void run() {
        }
    }

    // *******************************************************
    // ********************   Actions  ***********************
    // *******************************************************

    public class SwitchToDemoAction extends AbstractAction {
        SwingSet2 swingset;
        DemoModule demo;

        public SwitchToDemoAction(SwingSet2 swingset, DemoModule demo) {
            super(demo.getName(), demo.getIcon());
            this.swingset = swingset;
            this.demo = demo;
        }

        public void actionPerformed(ActionEvent e) {
            swingset.setDemo(demo);
        }
    }

    class OkAction extends AbstractAction {
        JDialog aboutBox;

        protected OkAction(JDialog aboutBox) {
            super("OkAction");
            this.aboutBox = aboutBox;
        }

        public void actionPerformed(ActionEvent e) {
            aboutBox.setVisible(false);
        }
    }

    class ChangeLookAndFeelAction extends AbstractAction {
        SwingSet2 swingset;
        String laf;

        protected ChangeLookAndFeelAction(SwingSet2 swingset, String laf) {
            super("ChangeTheme");
            this.swingset = swingset;
            this.laf = laf;
        }

        public void actionPerformed(ActionEvent e) {
            swingset.setLookAndFeel(laf);
        }
    }

    class ActivatePopupMenuAction extends AbstractAction {
        SwingSet2 swingset;
        JPopupMenu popup;

        protected ActivatePopupMenuAction(SwingSet2 swingset,
                JPopupMenu popup) {
            super("ActivatePopupMenu");
            this.swingset = swingset;
            this.popup = popup;
        }

        public void actionPerformed(ActionEvent e) {
            Dimension invokerSize = getSize();
            Dimension popupSize = popup.getPreferredSize();
            popup.show(swingset, (invokerSize.width - popupSize.width) / 2,
                    (invokerSize.height - popupSize.height) / 2);
        }
    }

    // Turns on all possible auditory feedback
    class OnAudioAction extends AbstractAction {
        SwingSet2 swingset;

        protected OnAudioAction(SwingSet2 swingset) {
            super("Audio On");
            this.swingset = swingset;
        }

        public void actionPerformed(ActionEvent e) {
            UIManager.put("AuditoryCues.playList",
                    UIManager.get("AuditoryCues.allAuditoryCues"));
            swingset.updateLookAndFeel();
        }
    }

    // Turns on the default amount of auditory feedback
    class DefaultAudioAction extends AbstractAction {
        SwingSet2 swingset;

        protected DefaultAudioAction(SwingSet2 swingset) {
            super("Audio Default");
            this.swingset = swingset;
        }

        public void actionPerformed(ActionEvent e) {
            UIManager.put("AuditoryCues.playList",
                    UIManager.get("AuditoryCues.defaultCueList"));
            swingset.updateLookAndFeel();
        }
    }

    // Turns off all possible auditory feedback
    class OffAudioAction extends AbstractAction {
        SwingSet2 swingset;

        protected OffAudioAction(SwingSet2 swingset) {
            super("Audio Off");
            this.swingset = swingset;
        }

        public void actionPerformed(ActionEvent e) {
            UIManager.put("AuditoryCues.playList",
                    UIManager.get("AuditoryCues.noAuditoryCues"));
            swingset.updateLookAndFeel();
        }
    }

    // Turns on or off the tool tips for the demo.
    class ToolTipAction extends AbstractAction {
        protected ToolTipAction() {
            super("ToolTip Control");
        }

        public void actionPerformed(ActionEvent e) {
            boolean status = ((JCheckBoxMenuItem) e.getSource()).isSelected();
            ToolTipManager.sharedInstance().setEnabled(status);
        }
    }

    class DragSupportAction extends AbstractAction {
        protected DragSupportAction() {
            super("DragSupport Control");
        }

        public void actionPerformed(ActionEvent e) {
            boolean dragEnabled = ((JCheckBoxMenuItem) e.getSource())
                    .isSelected();
            if (isApplet()) {
                setDragEnabled(dragEnabled);
            } else {
                for (SwingSet2 ss : swingSets) {
                    ss.setDragEnabled(dragEnabled);
                }
            }
        }
    }

    class ChangeThemeAction extends AbstractAction {
        SwingSet2 swingset;
        MetalTheme metalThemeObj;
        String napkinThemeName;

        protected ChangeThemeAction(SwingSet2 swingset, MetalTheme theme) {
            super("ChangeTheme");
            this.swingset = swingset;
            this.metalThemeObj = theme;
        }

        protected ChangeThemeAction(SwingSet2 swingset,
                String theme) {
            super("ChangeTheme");
            this.swingset = swingset;
            this.napkinThemeName = theme;
        }

        public void actionPerformed(ActionEvent e) {
            if (metalThemeObj != null)
                MetalLookAndFeel.setCurrentTheme(metalThemeObj);
            else if (napkinThemeName != null)
                NapkinTheme.Manager.setCurrentTheme(napkinThemeName);
            swingset.updateLookAndFeel();
        }
    }

    class ExitAction extends AbstractAction {
        SwingSet2 swingset;

        protected ExitAction(SwingSet2 swingset) {
            super("ExitAction");
            this.swingset = swingset;
        }

        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }

    class AboutAction extends AbstractAction {
        SwingSet2 swingset;

        protected AboutAction(SwingSet2 swingset) {
            super("AboutAction");
            this.swingset = swingset;
        }

        public void actionPerformed(ActionEvent e) {
            if (aboutBox == null) {
                // JPanel panel = new JPanel(new BorderLayout());
                JPanel panel = new AboutPanel(swingset);
                panel.setLayout(new BorderLayout());

                aboutBox = new JDialog(swingset.getFrame(),
                        getString("AboutBox.title"), false);
                aboutBox.setResizable(false);
                aboutBox.getContentPane().add(panel, BorderLayout.CENTER);

                // JButton button = new JButton(getString("AboutBox.ok_button_text"));
                JPanel buttonpanel = new JPanel();
                buttonpanel.setOpaque(false);
                JButton button = (JButton) buttonpanel.add(
                        new JButton(getString("AboutBox.ok_button_text"))
                );
                panel.add(buttonpanel, BorderLayout.SOUTH);

                button.addActionListener(new OkAction(aboutBox));
            }
            aboutBox.pack();
            Point p = swingset.getLocationOnScreen();
            aboutBox.setLocation(p.x + 10, p.y + 10);
            aboutBox.setVisible(true);
        }
    }

    class MultiScreenAction extends AbstractAction {
        static final int ALL_SCREENS = -1;
        int screen;

        protected MultiScreenAction(SwingSet2 swingset, int screen) {
            super("MultiScreenAction");
            this.screen = screen;
        }

        public void actionPerformed(ActionEvent e) {
            GraphicsDevice[] gds = GraphicsEnvironment.
                    getLocalGraphicsEnvironment().
                    getScreenDevices();
            if (screen == ALL_SCREENS) {
                for (int i = 0; i < gds.length; i++) {
                    SwingSet2 swingset = new SwingSet2(null,
                            gds[i].getDefaultConfiguration());
                    swingset.setDragEnabled(dragEnabled);
                }
            } else {
                SwingSet2 swingset = new SwingSet2(null,
                        gds[screen].getDefaultConfiguration());
                swingset.setDragEnabled(dragEnabled);
            }
        }
    }

    // *******************************************************
    // **********************  Misc  *************************
    // *******************************************************

    class DemoLoadThread extends Thread {
        SwingSet2 swingset;

        public DemoLoadThread(SwingSet2 swingset) {
            this.swingset = swingset;
        }

        public void run() {
            swingset.loadDemos();
            // The following line is useful for testing Right-to-Left display
            //swingset.frame.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
    }

    class AboutPanel extends JPanel {
        ImageIcon aboutimage = null;
        SwingSet2 swingset = null;

        public AboutPanel(SwingSet2 swingset) {
            this.swingset = swingset;
            aboutimage = swingset.createImageIcon("About.jpg",
                    "AboutBox.accessible_description");
            setOpaque(false);
        }

        public void paint(Graphics g) {
            aboutimage.paintIcon(this, g, 0, 0);
            paintChildren(g);
        }

        public Dimension getPreferredSize() {
            return new Dimension(aboutimage.getIconWidth(),
                    aboutimage.getIconHeight());
        }
    }

    private class ChangeFontAction extends AbstractAction {
        private SwingSet2 swingset;
        private boolean plain;

        ChangeFontAction(SwingSet2 swingset, boolean plain) {
            super("FontMenu");
            this.swingset = swingset;
            this.plain = plain;
        }

        public void actionPerformed(ActionEvent e) {
            if (plain) {
                UIManager.put("swing.boldMetal", Boolean.FALSE);
            } else {
                UIManager.put("swing.boldMetal", Boolean.TRUE);
            }
            // Change the look and feel to force the settings to take effect.
            updateLookAndFeel();
        }
    }
}

