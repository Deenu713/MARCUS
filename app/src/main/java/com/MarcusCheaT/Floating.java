package com.MarcusCheaT;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Base64;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;

public class Floating extends Service {
    WindowManager windowManager;

    int screenWidth, screenHeight, type, screenDpi;
    float density;

    WindowManager.LayoutParams iconLayoutParams, mainLayoutParams, canvasLayoutParams;
    RelativeLayout iconLayout;
    LinearLayout mainLayout;
    CanvasView canvasLayout;

    RelativeLayout closeLayout, maximizeLayout, minimizeLayout;
    RelativeLayout.LayoutParams closeLayoutParams, maximizeLayoutParams, minimizeLayoutParams;

    ImageView iconImg;

    String[] listTab = {"𝙴𝚂𝙿 𝙼𝙴𝙽𝚄", "𝙰𝙸𝙼 𝙼𝙴𝙽𝚄", "𝙷𝙴𝙻𝙿𝙴𝚁", "𝚂𝙴𝚃𝚃𝙸𝙽𝙶𝚂", "𝙸𝙽𝙵𝙾"};
    LinearLayout[] pageLayouts = new LinearLayout[listTab.length];
    int lastSelectedPage = 0;

    SharedPreferences configPrefs;
    long sleepTime = 1000 / 60;

    boolean isMaximized = false;
    int lastMaximizedX = 0, lastMaximizedY = 0;
    int lastMaximizedW = 0, lastMaximizedH = 0;

    int layoutWidth;
    int layoutHeight;
    int iconSize;
    int menuButtonSize;
    int tabWidth;
    int tabHeight;

    float mediumSize = 5.0f;

    private static final int RadioColor = 0;

    private native void onSendConfig(String s, String v);
    static native  void Switch(int i,boolean jboolean1);
    private native void onCanvasDraw(Canvas canvas, int w, int h, float d);

    void CreateCanvas() {
        final int FLAGS = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS |
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

        canvasLayoutParams = new WindowManager.LayoutParams(screenWidth, screenHeight, type, FLAGS, PixelFormat.RGBA_8888);

        canvasLayoutParams.x = 0;
        canvasLayoutParams.y = 0;
        canvasLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            canvasLayoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        canvasLayout = new CanvasView(this);
        windowManager.addView(canvasLayout, canvasLayoutParams);
    }

    private class CanvasView extends View {
        public CanvasView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            try {
                onCanvasDraw(canvas, screenWidth, screenHeight, density);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void UpdateConfiguration(String s, Object v) {
        try {
            onSendConfig(s, v.toString());

            SharedPreferences.Editor configEditor = configPrefs.edit();
            configEditor.putString(s, v.toString());
            configEditor.apply();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mUpdateCanvas.isAlive()) {
            mUpdateCanvas.interrupt();
        }
        if (mUpdateThread.isAlive()) {
            mUpdateThread.interrupt();
        }

        if (iconLayout != null) {
            windowManager.removeView(iconLayout);
        }
        if (mainLayout != null) {
            windowManager.removeView(mainLayout);
        }
        if (canvasLayout != null) {
            windowManager.removeView(canvasLayout);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        configPrefs = getSharedPreferences("config", MODE_PRIVATE);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        Point screenSize = new Point();
        Display display = windowManager.getDefaultDisplay();
        display.getRealSize(screenSize);

        screenWidth = screenSize.x;
        screenHeight = screenSize.y;
        screenDpi = getResources().getDisplayMetrics().densityDpi;

        density = getResources().getDisplayMetrics().density;

        layoutWidth = convertSizeToDp(320);
        layoutHeight = convertSizeToDp(340);
        iconSize = convertSizeToDp(240);
        menuButtonSize = convertSizeToDp(25);
        tabWidth = convertSizeToDp(64);
        tabHeight = convertSizeToDp(35);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            type = 2038;
        } else {
            type = 2002;
        }

        CreateIcon();
        CreateLayout();
        CreateCanvas();

        mUpdateThread.start();
        mUpdateCanvas.start();
    }

    void AddFeatures() {
       
		AddText(0, "Anticheat Hook:", 7.f, Color.BLACK);
		
		
		AddCheckbox(0, "ᴍᴛᴩ ʜᴏᴏᴋᴇᴅ ʙyᴩᴀꜱꜱ", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    UpdateConfiguration("1", isChecked ? 1 : 0);
                }
            });
        AddText(0, "ESP Generic Adjustment:️", 6.5f, Color.BLACK);
        AddCheckbox(0, "ʟɪɴᴇ", false, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                UpdateConfiguration("ESP::LINE", isChecked ? 1 : 0);
            }
        });
      /*  AddCheckbox(0, "ʙᴏx", false, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                UpdateConfiguration("ESP::BOX", isChecked ? 1 : 0);
            }
        });
        */
        AddCheckbox(0, "ʜᴇᴀʟᴛʜ", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    UpdateConfiguration("ESP::HEALTH", isChecked ? 1 : 0);
                }
            });
        
        AddText(0, "Customize Box", 7.0f, Color.BLACK);
        AddRadioButton(0, new String[]{"ᴅɪꜱᴀʙʟᴇ" , "ʀᴇᴅ ʙᴏx", "ʙʟᴀᴄᴋ ʙᴏx", "ᴡʜɪᴛᴇ ʙᴏx","ɢʀᴇᴇɴ ʙᴏx","yᴇʟʟᴏᴡ ʙᴏx",}, 0, new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    UpdateConfiguration("ESP::BOX", checkedId);
                }
            });
        /*AddText(0, "HEALTH OPTIONS: ", 7.0f, Color.RED);
        AddRadioButton(0, new String[]{"ɴᴏʀᴍᴀʟ" , "ᴠᴇʀᴛɪᴄᴀʟ", "ʜᴏʀɪᴢᴏɴᴛᴀʟ",}, 0, new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    UpdateConfiguration("1", checkedId);
                }
            });
        
        */
        AddCheckbox(0, "ɴᴀᴍᴇ", false, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                UpdateConfiguration("ESP::NAME", isChecked ? 1 : 0);
            }
        });
        
        AddCheckbox(0, "ꜱᴋᴇʟᴇᴛᴏɴ | ʙᴇᴛᴀ ᴍᴀy ᴄʀᴀꜱʜ", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    UpdateConfiguration("ESP::SKELETON", isChecked ? 1 : 0);
                }
            });
            
        
        
        AddCheckbox(0, "ᴅɪꜱᴛᴀɴᴄᴇ", false, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                UpdateConfiguration("ESP::DISTANCE", isChecked ? 1 : 0);
            }
        });
        
        
        
        
        AddText(0, "ESP World:️", 7.f, Color.BLACK);
        AddCheckbox(0, "ᴀʟʟ ᴠᴇʜɪᴄʟᴇꜱ", false, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                UpdateConfiguration("ESP::VEHICLE", isChecked ? 1 : 0);
            }
        });
        AddCheckbox(0, "ɢʀᴇɴᴀᴅᴇ ᴡᴀʀɴɪɴɢ", false, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                UpdateConfiguration("ESP::GRENADE", isChecked ? 1 : 0);
            }
        });
		
        AddCheckbox(0, "ʟᴏᴏᴛ ʙᴏx", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    UpdateConfiguration("LootBox", isChecked ? 1 : 0);
                }
            });
		
            
            
        AddText(1, "Aim Adjustment:️", 7.f, Color.BLACK);

     /*   AddCheckbox(1, "ꜰᴏᴠ ᴀɪᴍʙᴏᴛ (ʀɪꜱᴋ)", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    UpdateConfiguration("AIM::AIMBOT", isChecked ? 1 : 0);
                }
            });


*/
        AddCheckbox(1, "ꜰᴏᴠ ʙᴜʟʟᴇᴛ ᴛʀᴀᴄᴋɪɴɢ", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    UpdateConfiguration("AIM::AIMBULLET", isChecked ? 1 : 0);
                }
            });
        AddText(1, "Aim Location: ", 6.0f, Color.BLACK);
        AddRadioButton(1, new String[]{"ʜᴇᴀᴅ", "ʙᴏᴅy"}, 0, new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    UpdateConfiguration("AIM::LOCATION", checkedId);
                }
            });
        AddText(1, "Aim Target: ", 6.0f, Color.BLACK);
        AddRadioButton(1, new String[]{"ᴄʟᴏꜱᴇꜱᴛ ᴛᴏ ᴅɪꜱᴛᴀɴᴄᴇ ᴩʀɪᴏʀɪᴛy", "ɪɴꜱɪᴅᴇ ᴄɪʀᴄʟᴇ ᴩʀɪᴏʀɪᴛy"}, 0, new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    UpdateConfiguration("AIM::TARGET", checkedId);
                }
            });
        AddSeekbar(1, "ꜰᴏᴠ ꜱɪᴢᴇ", 0, 500, 0, "", "", new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    UpdateConfiguration("AIM::SIZE", progress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        AddText(1, "Aim Trigger: ", 6.0f, Color.BLACK);
        AddRadioButton(1, new String[]{"ɴᴏᴛʜɪɴɢ" , "ꜱʜᴏᴏᴛɪɴɢ", "ᴀɪᴍɪɴɢ", "ꜱʜᴏᴏᴛɪɴɢ & ᴀɪᴍɪɴɢ",}, 0, new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    UpdateConfiguration("AIM::TRIGGER", checkedId);
                }
            });
        AddCheckbox(1, "ᴩʀᴇᴅɪᴄᴛɪᴏɴ", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    UpdateConfiguration("AIM::PREDICTION", isChecked ? 1 : 0);
                }
            });
        AddCheckbox(1, "ᴀɪᴍ ᴋɴᴏᴄᴋᴇᴅ", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    UpdateConfiguration("AIM::KNOCKED", isChecked ? 1 : 0);
                }
            });
        AddCheckbox(1, "ᴠɪꜱɪʙɪʟɪᴛy ᴄʜᴇᴄᴋ", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    UpdateConfiguration("AIM::VISCHECK", isChecked ? 1 : 0);
                }
            });

        AddSeekbar(1, "ꜱᴍᴏᴏᴛʜɴᴇꜱꜱ", 0, 200, 0, "", "", new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    UpdateConfiguration("m", progress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

        
        
		
		//*AddText(2, "⚠️ We have removed risky MEMORY Features because SERVER BASED DETECTION is High Now!", 6.5f, Color.RED);*//
  
		AddText(2, "Helper Menu:", 7.f, Color.BLACK);
        
        
        
		
		
		
		AddCheckbox(2, "ᴇɴᴀʙʟᴇ ʀᴇᴄᴏɪʟ", false, new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

					Switch(4, isChecked);
				}
			}); 
            
            
        AddCheckbox(2, "ᴀɪᴍʙᴏᴛ 180°", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Switch(7, isChecked);
                }
			}); 
            
                    
        AddCheckbox(2, "x ʜɪᴛ ᴇꜰꜰᴇᴄᴛ", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Switch(27, isChecked);
                }
			}); 
            
            AddCheckbox(2, "ᴡᴀʟʟ ʟᴏᴏᴛ", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Switch(21, isChecked);
                }
			}); 

        AddCheckbox(2, "ʙʟᴀᴄᴋ ʙᴏᴅy", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Switch(5, isChecked);
                }
			}); 
            
            
        AddCheckbox(2, "ʙʟᴀᴄᴋ ꜱᴋy", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Switch(6, isChecked);
                }
			}); 
            
            
        AddCheckbox(2, "ʀᴇᴍᴏᴠᴇ ꜰᴏɢ", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Switch(9, isChecked);
                }
			}); 
            
        AddCheckbox(2, "ꜱᴛᴀᴛᴜᴇ ᴩʟᴀyᴇʀ", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Switch(14, isChecked);
                }
			}); 
            
        
			
        AddCheckbox(2, "ᴅᴇꜱᴇʀᴛ ᴍᴀᴩ", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Switch(16, isChecked);
                }
			}); 
            
        AddCheckbox(2, "ʀᴇᴅ ꜰᴏɢ", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Switch(30, isChecked);
                }
			}); 
            
   /*     AddCheckbox(2, "ʙʟᴀᴄᴋ ꜰᴏɢ", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Switch(31, isChecked);
                }
			}); 
            
            
        AddCheckbox(2, "ɢʀᴇᴇɴ ꜰᴏɢ", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Switch(32, isChecked);
                }
			}); 
            
            
        AddCheckbox(2, "ᴅᴀʀᴋ ʙʟᴜᴇ ꜰᴏɢ", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Switch(33, isChecked);
                }
			}); 
            
        AddCheckbox(2, "ᴄyᴀɴ ꜰᴏɢ", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Switch(34, isChecked);
                }
			}); 
            
        AddCheckbox(2, "ᴩᴜʀᴩʟᴇ ꜰᴏɢ", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Switch(35, isChecked);
                }
			}); 
			
			*/
        AddCheckbox(2, "ʟɪᴛᴇ ɢʀᴇᴇɴ ꜰᴏɢ", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Switch(36, isChecked);
                }
			}); 
        AddCheckbox(2, "ɴᴏ ᴛʀᴇᴇ", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Switch(17, isChecked);
                }
			}); 
        
            AddCheckbox(2, "ꜰʟᴀꜱʜ [ʙᴜɢ]", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Switch(13, isChecked);
                }
			}); 
            
        AddText(2, "# HIGH RISK, ROOT ONLY #:", 7.f, Color.BLACK);
        
        AddCheckbox(2, "ɴᴏ ꜱʜᴀᴋᴇ", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Switch(2, isChecked);
                }
			}); 
			
        
    /*    AddCheckbox(2, "ᴀɪᴍʙᴏᴛ 360°", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Switch(37, isChecked);
                }
			}); 
        
        */
        
        AddCheckbox(2, "ɪɴꜱᴛᴀɴᴛ ʜɪᴛ", false, new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

					Switch(38, isChecked);
				}
			}); 
        
        AddCheckbox(2, "ᴍᴀɢɢɪᴄ ʙᴜʟʟᴇᴛ", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Switch(22, isChecked);
                }
            }); 

        
      /*  AddCheckbox(2, "ᴡᴀʟʟ ꜱʜᴏᴛ", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Switch(29, isChecked);
                }
			}); 

*/
        AddCheckbox(2, "ꜰʟᴀꜱʜ ᴠ2 | ʟᴀɢ", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Switch(23, isChecked);
                }
			}); 
            
        AddCheckbox(2, "ꜱʟᴏᴡ ᴍᴏᴛɪᴏɴ", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Switch(19, isChecked);
                }
			}); 
			
			AddCheckbox(2, "ꜰɪx ꜱᴛᴜᴄᴋ", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Switch(18, isChecked);
                }
			}); 
            
            
        AddCheckbox(2, "ᴀɴᴛɪ ᴩᴜʟʟ", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Switch(20, isChecked);
                }
			}); 
            
            
        AddCheckbox(2, "ꜱᴩᴇᴇᴅ ᴋɴᴏᴄᴋᴇᴅ", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Switch(26, isChecked);
                }
			}); 
            
        AddCheckbox(2, "ʟᴏɴɢ ᴊᴜᴍᴩ", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Switch(12, isChecked);
                }
			}); 
            
        
        AddCheckbox(2, "ᴄᴀʀ ꜱᴩᴇᴇᴅ", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Switch(10, isChecked);
                }
			}); 
            
        AddCheckbox(2, "ᴄᴀʀ ᴊᴜᴍᴩ", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Switch(11, isChecked);
                }
			}); 
            
        AddCheckbox(2, "ᴀᴜᴛᴏ ᴅʀɪᴠᴇ ᴍᴏᴅᴇ", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Switch(28, isChecked);
                }
			}); 
            
    /*    AddCheckbox(2, "ꜱᴛᴀɴᴅ ꜱᴄᴏᴩᴇ ", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Switch(24, isChecked);
                }
			}); 
			
			*/
        AddCheckbox(2, "ᴩʀᴏɴᴇ ꜱᴄᴏᴩᴇ", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Switch(25, isChecked);
                }
			}); 

        
        
            
        
        
        
            
        
        
		 
		 
                                                                                                                                                                                                        AddText(4, "Info:", 7.f, Color.BLACK);
                                                                                                                                                                                                                                 AddText(4, "ᴛᴇʟᴇɢʀᴀᴍ ᴄʜᴀɴɴᴇʟ: ★@ᴍᴀʀᴄᴜꜱᴄʜᴇᴀᴛ★", 5.f, Color.BLACK);
                                                                                                                                                                                                                                 AddText(4, "ᴅᴇᴠᴇʟᴏᴩᴇʀ: ★@ᴍᴀʀᴄᴜꜱᴏᴡɴᴇʀ★", 5.f, Color.BLACK);
                                                                                                                                                                                                                                 AddText(4, "         ©Copyright | @MarcuscheaT", 5.f, Color.BLACK);
         
        
        AddText(3, "Setting Menu:", 7.f, Color.BLACK);
        
        
        AddCheckbox(3, "ʟᴏᴡ ɢʀᴀᴩʜɪᴄꜱ ᴍᴏᴅᴇ", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    UpdateConfiguration("AIM::AIMBULLET", isChecked ? 1 : 0);
                }
            });
            
        AddCheckbox(3, "ʜɪᴅᴇ ꜰᴩꜱ", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    UpdateConfiguration("AIM::AIMBULLET", isChecked ? 1 : 0);
                }
            });
        AddCheckbox(3, "ʜɪᴅᴇ *ᴛᴀʀɢᴇᴛ ᴛᴇꜱᴛ", false, new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    UpdateConfiguration("AIM::AIMBULLET", isChecked ? 1 : 0);
                }
            });
            
        AddSeekbar(3, "ᴍᴏᴅ ᴍᴇɴᴜ ᴏᴩᴀᴄɪᴛy", 1, 100, 100, "", "%", new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mainLayout.setAlpha((float) progress / 100.f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        AddSeekbar(3, "ɪᴄᴏɴ ꜱɪᴢᴇ", 50, 200, 100, "", "%", new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ViewGroup.LayoutParams iconParams = iconImg.getLayoutParams();
                iconParams.width = (int) ((float) 75 * ((float) progress / 100.f));
                iconParams.height = (int) ((float) 75 * ((float) progress / 100.f));
                iconImg.setLayoutParams(iconParams);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                iconLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                iconLayout.setVisibility(View.GONE);
            }
        });
        AddSeekbar(3, "ɪᴄᴏɴ ᴏᴩᴀᴄɪᴛy", 0, 100, 100, "", "%", new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                iconLayout.setAlpha((float) progress / 100.f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                iconLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                iconLayout.setVisibility(View.GONE);
            }
        });
        
        
        AddText(3, "Framerate:", 7.f, Color.BLACK);
        AddRadioButton(3, new String[]{"30ꜰᴩꜱ","45ꜰᴩꜱ","60ꜰᴩꜱ","90ꜰᴩꜱ"}, 1, new RadioGroup.OnCheckedChangeListener(){
                public void onCheckedChanged(RadioGroup radioGroup, int x) {

                }
            });
        
        AddCheckbox(Integer.valueOf(3), "ʀᴇꜱᴇᴛꜱ ɢᴜᴇꜱᴛ", false, new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    GetExecute("/reset.sh");
                }

                private void GetExecute(String р0) {
                }
			});
        
        AddButton(3,"Clear Logs",ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 15, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String logsStr = "/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Logs";
                    File logs = new File(getExternalFilesDir(null).getPath() + logsStr);
                    if (logs.isDirectory() && logs.exists() && logs != null){
                        delete(logs.toString());
                    }
                }

        private void delete(String toString)
        {
        }
            });
        
        
        AddButton(3, "Save Profile", ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 15, new View.OnClickListener() {
                public void onClick(View view) {
                    SaveConfiguration();
                    Toast.makeText(Floating.this, "Profile saved!", 1).show();
                }

                private void SaveConfiguration() {
                }
			});
        
    }

    private void AddText(int р0, String р1, int р2, int р3, String р4) {
    }

    @SuppressLint("ClickableViewAccessibility")
    void CreateLayout() {
        mainLayoutParams = new WindowManager.LayoutParams(layoutWidth, layoutHeight, type, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, PixelFormat.RGBA_8888);

        mainLayoutParams.x = 0;
        mainLayoutParams.y = 0;
        mainLayoutParams.gravity = Gravity.START | Gravity.TOP;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mainLayoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        GradientDrawable mainLayoutBg = new GradientDrawable();
        mainLayoutBg.setColor(0xFFFFFFFF);
        mainLayout.setBackground(mainLayoutBg);

        RelativeLayout headerLayout = new RelativeLayout(this);
        headerLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, menuButtonSize + convertSizeToDp(4)));
        headerLayout.setClickable(true);
        headerLayout.setFocusable(true);
        headerLayout.setFocusableInTouchMode(true);
        headerLayout.setBackgroundColor(Color.argb(255,211,211,211));
        mainLayout.addView(headerLayout);

        TextView textTitle = new TextView(this);
        textTitle.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        textTitle.setGravity(Gravity.CENTER);
        textTitle.setClickable(true);
        textTitle.setFocusable(true);
        textTitle.setFocusableInTouchMode(true);
        textTitle.setText(" ᴍᴀʀᴄᴜꜱᴄʜᴇᴀᴛ~ᴠɪᴩ");
        textTitle.setTextSize(convertSizeToDp(6.5f));
        textTitle.setTextColor(Color.BLACK);
        headerLayout.addView(textTitle);

        View.OnTouchListener onTitleListener = new View.OnTouchListener() {
            float pressedX;
            float pressedY;
            float deltaX;
            float deltaY;
            float newX;
            float newY;
            float maxX;
            float maxY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:

                        deltaX = mainLayoutParams.x - event.getRawX();
                        deltaY = mainLayoutParams.y - event.getRawY();

                        pressedX = event.getRawX();
                        pressedY = event.getRawY();

                        break;
                    case MotionEvent.ACTION_MOVE:
                        newX = event.getRawX() + deltaX;
                        newY = event.getRawY() + deltaY;

                        maxX = screenWidth - mainLayout.getWidth();
                        maxY = screenHeight - mainLayout.getHeight();

                        if (newX < 0)
                            newX = 0;
                        if (newX > maxX)
                            newX = (int) maxX;
                        if (newY < 0)
                            newY = 0;
                        if (newY > maxY)
                            newY = (int) maxY;

                        mainLayoutParams.x = (int) newX;
                        mainLayoutParams.y = (int) newY;
                        windowManager.updateViewLayout(mainLayout, mainLayoutParams);

                        break;

                    default:
                        break;
                }

                return false;
            }
        };

        headerLayout.setOnTouchListener(onTitleListener);
        textTitle.setOnTouchListener(onTitleListener);

        closeLayout = new RelativeLayout(this);
        closeLayoutParams = new RelativeLayout.LayoutParams(menuButtonSize, menuButtonSize);
        closeLayout.setLayoutParams(closeLayoutParams);
        closeLayout.setX(mainLayoutParams.width - closeLayoutParams.width - convertSizeToDp(2));
        closeLayout.setY(convertSizeToDp(2));
        closeLayout.setBackgroundColor(Color.argb(255, 0, 0, 0));
        headerLayout.addView(closeLayout);

        maximizeLayout = new RelativeLayout(this);
        maximizeLayoutParams = new RelativeLayout.LayoutParams(menuButtonSize, menuButtonSize);
        maximizeLayout.setLayoutParams(maximizeLayoutParams);
        maximizeLayout.setX(closeLayout.getX() - maximizeLayoutParams.width - convertSizeToDp(2));
        maximizeLayout.setY(convertSizeToDp(2));
        maximizeLayout.setBackgroundColor(Color.argb(255, 0, 0, 0));
        headerLayout.addView(maximizeLayout);

        minimizeLayout = new RelativeLayout(this);
        minimizeLayoutParams = new RelativeLayout.LayoutParams(menuButtonSize, menuButtonSize);
        minimizeLayout.setLayoutParams(minimizeLayoutParams);
        minimizeLayout.setX(maximizeLayout.getX() - minimizeLayoutParams.width - convertSizeToDp(2));
        minimizeLayout.setY(convertSizeToDp(2));
        minimizeLayout.setBackgroundColor(Color.argb(255, 0, 0, 0));
        headerLayout.addView(minimizeLayout);

        closeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Floating.this, 5);
                builder.setTitle("Marcus Mod");
                builder.setMessage("Are you sure you want to stop the Hack?\nYou won't be to able access the Hack again until you re-open the Game!");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        stopSelf();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.getWindow().setType(type);
                dialog.show();
            }
        });

        maximizeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMaximized = !isMaximized;

                if (isMaximized) {
                    lastMaximizedX = mainLayoutParams.x;
                    lastMaximizedY = mainLayoutParams.y;

                    lastMaximizedW = mainLayoutParams.width;
                    lastMaximizedH = mainLayoutParams.height;

                    mainLayoutParams.x = 0;
                    mainLayoutParams.y = 0;

                    mainLayoutParams.width = screenWidth;
                    mainLayoutParams.height = screenHeight;
                    windowManager.updateViewLayout(mainLayout, mainLayoutParams);

                    closeLayout.setX(mainLayoutParams.width - closeLayoutParams.width - (closeLayoutParams.width * 0.075f));
                    maximizeLayout.setX(closeLayout.getX() - maximizeLayoutParams.width - (maximizeLayoutParams.width * 0.075f));
                    minimizeLayout.setX(maximizeLayout.getX() - minimizeLayoutParams.width - (minimizeLayoutParams.width * 0.075f));
                } else {
                    mainLayoutParams.x = lastMaximizedX;
                    mainLayoutParams.y = lastMaximizedY;

                    mainLayoutParams.width = lastMaximizedW;
                    mainLayoutParams.height = lastMaximizedH;
                    windowManager.updateViewLayout(mainLayout, mainLayoutParams);

                    closeLayout.setX(mainLayoutParams.width - closeLayoutParams.width - (closeLayoutParams.width * 0.075f));
                    maximizeLayout.setX(closeLayout.getX() - maximizeLayoutParams.width - (maximizeLayoutParams.width * 0.075f));
                    minimizeLayout.setX(maximizeLayout.getX() - minimizeLayoutParams.width - (minimizeLayoutParams.width * 0.075f));
                }
            }
        });

        minimizeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainLayout.setVisibility(View.GONE);
                iconLayout.setVisibility(View.VISIBLE);
            }
        });

        TextView closeText = new TextView(this);
        closeText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        closeText.setGravity(Gravity.CENTER);
        closeText.setText("✕");
        closeText.setTextSize(convertSizeToDp(mediumSize));
        closeText.setTextColor(Color.RED);
        closeLayout.addView(closeText);

        TextView maximizeText = new TextView(this);
        maximizeText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        maximizeText.setGravity(Gravity.CENTER);
        maximizeText.setText("□");
        maximizeText.setTextSize(convertSizeToDp(mediumSize));
        maximizeText.setTextColor(Color.RED);
        maximizeLayout.addView(maximizeText);

        TextView minimizeText = new TextView(this);
        minimizeText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        minimizeText.setGravity(Gravity.CENTER);
        minimizeText.setText("—");
        minimizeText.setTextSize(convertSizeToDp(mediumSize));
        minimizeText.setTextColor(Color.RED);
        minimizeLayout.addView(minimizeText);

        LinearLayout tabLayout = new LinearLayout(this);
        tabLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tabLayout.setOrientation(LinearLayout.HORIZONTAL);

        HorizontalScrollView tabScrollView = new HorizontalScrollView(this);
        tabScrollView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tabScrollView.setBackgroundColor(Color.argb(255, 0, 0, 0));

        tabScrollView.addView(tabLayout);

        mainLayout.addView(tabScrollView);

        final RelativeLayout[] tabButtons = new RelativeLayout[listTab.length];
        for (int i = 0; i < tabButtons.length; i++) {
            pageLayouts[i] = new LinearLayout(this);
            pageLayouts[i].setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            pageLayouts[i].setOrientation(LinearLayout.VERTICAL);

            ScrollView scrollView = new ScrollView(this);
            scrollView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            scrollView.addView(pageLayouts[i]);

            tabButtons[i] = new RelativeLayout(this);
            tabButtons[i].setLayoutParams(new RelativeLayout.LayoutParams(tabWidth, tabHeight));
            if (i != 0) {
                tabButtons[i].setBackgroundColor(Color.argb(255, 0, 0, 0));
                pageLayouts[i].setVisibility(View.GONE);
            } else {
                tabButtons[i].setBackgroundColor(Color.RED);
                pageLayouts[i].setVisibility(View.VISIBLE);
            }

            TextView tabText = new TextView(this);
            tabText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            tabText.setGravity(Gravity.CENTER);
            tabText.setText(listTab[i]);
            tabText.setTextSize(convertSizeToDp(4.0f));
            tabText.setTextColor(Color.WHITE);
            tabButtons[i].addView(tabText);

            final int curTab = i;
            tabButtons[i].setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (curTab != lastSelectedPage) {
							tabButtons[curTab].setBackgroundColor(Color.RED);
							pageLayouts[curTab].setVisibility(View.VISIBLE);

							pageLayouts[lastSelectedPage].setVisibility(View.GONE);
							lastSelectedPage = curTab;

							for (int j = 0; j < tabButtons.length; j++) {
								if (j != curTab) {
									tabButtons[j].setBackgroundColor(Color.argb(255, 0, 0, 0));
								}
							}
						}
					}
				});

            tabLayout.addView(tabButtons[i]);
            mainLayout.addView(scrollView);
        }

        windowManager.addView(mainLayout, mainLayoutParams);

        AddFeatures();
    }


    @SuppressLint("ClickableViewAccessibility")
    void CreateIcon() {
        iconLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams iconParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        iconLayout.setLayoutParams(iconParams);

        iconImg = new ImageView(this);
        ViewGroup.LayoutParams iconImgParams = new ViewGroup.LayoutParams(150, 150);
        iconImg.setLayoutParams(iconImgParams);

        iconLayout.addView(iconImg);

        try {
            String iconBase64 = "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";
            byte[] iconData = Base64.decode(iconBase64, Base64.DEFAULT);

            Bitmap bmp = BitmapFactory.decodeByteArray(iconData, 0, iconData.length);
            iconImg.setImageBitmap(bmp);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        iconLayoutParams = new WindowManager.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, type, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, PixelFormat.TRANSLUCENT);
        iconLayoutParams.gravity = Gravity.START | Gravity.TOP;

        iconLayoutParams.x = 0;
        iconLayoutParams.y = 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            iconLayoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        iconLayout.setVisibility(View.GONE);

        iconLayout.setOnTouchListener(new View.OnTouchListener() {
            float pressedX;
            float pressedY;
            float deltaX;
            float deltaY;
            float newX;
            float newY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:

                        deltaX = iconLayoutParams.x - event.getRawX();
                        deltaY = iconLayoutParams.y - event.getRawY();

                        pressedX = event.getRawX();
                        pressedY = event.getRawY();

                        break;
                    case MotionEvent.ACTION_UP:
                        int Xdiff = (int) (event.getRawX() - pressedX);
                        int Ydiff = (int) (event.getRawY() - pressedY);

                        if (Xdiff == 0 && Ydiff == 0) {
                            mainLayout.setVisibility(View.VISIBLE);
                            iconLayout.setVisibility(View.GONE);
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        newX = event.getRawX() + deltaX;
                        newY = event.getRawY() + deltaY;

                        float maxX = screenWidth - v.getWidth();
                        float maxY = screenHeight - v.getHeight();

                        if (newX < 0)
                            newX = 0;
                        if (newX > maxX)
                            newX = (int) maxX;
                        if (newY < 0)
                            newY = 0;
                        if (newY > maxY)
                            newY = (int) maxY;

                        iconLayoutParams.x = (int) newX;
                        iconLayoutParams.y = (int) newY;

                        windowManager.updateViewLayout(iconLayout, iconLayoutParams);
                        break;

                    default:
                        break;
                }

                return false;
            }
        });

        windowManager.addView(iconLayout, iconLayoutParams);
    }

    LinearLayout CreateHolder(Object data) {
        RelativeLayout parentHolder = new RelativeLayout(this);
        parentHolder.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        LinearLayout childHolder = new LinearLayout(this);
        childHolder.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        childHolder.setOrientation(LinearLayout.HORIZONTAL);
        parentHolder.addView(childHolder);

        if (data instanceof Integer)
            pageLayouts[(Integer) data].addView(parentHolder);
        else if (data instanceof ViewGroup)
            ((ViewGroup) data).addView(parentHolder);

        return childHolder;
    }

    void AddText(Object data, String text, float size, int color) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextColor(color);
        textView.setPadding(15, 15, 15, 15);
        textView.setTextSize(convertSizeToDp(size));
        textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        if (data instanceof Integer)
            pageLayouts[(Integer) data].addView(textView);
        else if (data instanceof ViewGroup)
            ((ViewGroup) data).addView(textView);
    }

    void AddCenteredText(Object data, String text, int size, int typeface, String color) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextColor(Color.parseColor(color));
        textView.setTypeface(null, typeface);
        textView.setPadding(15, 15, 15, 15);
        textView.setTextSize(convertSizeToDp(size));
        textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setGravity(Gravity.CENTER);

        if (data instanceof Integer)
            pageLayouts[(Integer) data].addView(textView);
        else if (data instanceof ViewGroup)
            ((ViewGroup) data).addView(textView);
    }

    void AddHeader(Object data, String text) {
        LinearLayout headerLayout = new LinearLayout(this);
        headerLayout.setLayoutParams(new WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        headerLayout.setOrientation(LinearLayout.VERTICAL);
        headerLayout.setBackgroundColor(Color.argb(255, 0, 0, 1));

        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextColor(Color.BLACK);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setPadding(15, 15, 15, 15);
        textView.setTextSize(convertSizeToDp(12.5f));
        textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        headerLayout.addView(textView);

        if (data instanceof Integer)
            pageLayouts[(Integer) data].addView(headerLayout);
        else if (data instanceof ViewGroup)
            ((ViewGroup) data).addView(headerLayout);
    }

    void AddCheckbox(Object data, String text, boolean checked, CompoundButton.OnCheckedChangeListener listener) {
        CheckBox checkBox = new CheckBox(this);
        checkBox.setText(text);
        checkBox.setTextSize(convertSizeToDp(4.f));
        checkBox.setTextColor(Color.BLACK);
        checkBox.setChecked(checked);
        checkBox.setOnCheckedChangeListener(listener);
        checkBox.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        if (Build.VERSION.SDK_INT >= 21) {
            ColorStateList colorStateList = new ColorStateList(
                    new int[][]{
                            new int[]{-android.R.attr.state_checked}, // unchecked
                            new int[]{android.R.attr.state_checked}  // checked
                    },
                    new int[]{
                    Color.BLACK,
                    Color.BLACK
                    }
            );
            checkBox.setButtonTintList(colorStateList);
        }

        if (data instanceof Integer)
            pageLayouts[(Integer) data].addView(checkBox);
        else if (data instanceof ViewGroup)
            ((ViewGroup) data).addView(checkBox);
    }

    void AddSwitch(Object data, String text, boolean checked, CompoundButton.OnCheckedChangeListener listener) {
        Switch toggle = new Switch(this);
        toggle.setText(text);
        toggle.setTextSize(convertSizeToDp(mediumSize));
        toggle.setTextColor(Color.BLACK);
        toggle.setChecked(checked);
        toggle.setPadding(15, 15, 15, 15);
        toggle.setOnCheckedChangeListener(listener);
        toggle.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        if (Build.VERSION.SDK_INT >= 21) {
            ColorStateList colorStateList = new ColorStateList(
                    new int[][]{
                            new int[]{-android.R.attr.state_checked}, // unchecked
                            new int[]{android.R.attr.state_checked}  // checked
                    },
                    new int[]{
                    Color.BLACK,
                    Color.BLACK
                    }
            );
            toggle.setButtonTintList(colorStateList);
        }

        if (data instanceof Integer)
            pageLayouts[(Integer) data].addView(toggle);
        else if (data instanceof ViewGroup)
            ((ViewGroup) data).addView(toggle);
    }

    void AddSeekbar(Object data, String text, int min, int max, int value, final String prefix, final String suffix, final SeekBar.OnSeekBarChangeListener listener) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView textV = new TextView(this);
        textV.setText(text + ":");
        textV.setTextSize(convertSizeToDp(mediumSize));
        textV.setPadding(15, 15, 15, 15);
        textV.setTextColor(Color.BLACK);
        textV.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textV.setGravity(Gravity.LEFT);

        SeekBar seekBar = new SeekBar(this);
        seekBar.setMax(max);
        if (Build.VERSION.SDK_INT >= 26) {
            seekBar.setMin(min);
            seekBar.setProgress(min);
        }
        if (Build.VERSION.SDK_INT >= 21) {
            seekBar.setThumbTintList(ColorStateList.valueOf(Color.BLACK));
            seekBar.setProgressTintList(ColorStateList.valueOf(Color.BLACK));
        }
        seekBar.setPadding(20, 15, 20, 15);

        final TextView textValue = new TextView(this);
        textValue.setText(prefix + min + suffix);
        textValue.setGravity(Gravity.RIGHT);
        textValue.setTextSize(convertSizeToDp(mediumSize));
        textValue.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textValue.setPadding(20, 15, 20, 15);
        textValue.setTextColor(Color.BLACK);

        final int minimValue = min;
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < minimValue) {
                    progress = minimValue;
                    seekBar.setProgress(progress);
                }

                if (listener != null) listener.onProgressChanged(seekBar, progress, fromUser);
                textValue.setText(prefix + progress + suffix);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (listener != null) listener.onStartTrackingTouch(seekBar);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (listener != null) listener.onStopTrackingTouch(seekBar);
            }
        });

        if (value != 0) {
            if (value < min)
                value = min;
            if (value > max)
                value = max;

            textValue.setText(prefix + value + suffix);
            seekBar.setProgress(value);
        }

        linearLayout.addView(textV);
        linearLayout.addView(textValue);

        if (data instanceof Integer) {
            pageLayouts[(Integer) data].addView(linearLayout);
            pageLayouts[(Integer) data].addView(seekBar);
        } else if (data instanceof ViewGroup) {
            ((ViewGroup) data).addView(linearLayout);
            ((ViewGroup) data).addView(seekBar);
        }
    }

    void AddRadioButton(Object data, String[] list, int defaultCheckedId, RadioGroup.OnCheckedChangeListener listener) {
        RadioGroup rg = new RadioGroup(this);
        RadioButton[] rb = new RadioButton[list.length];
        rg.setOrientation(RadioGroup.VERTICAL);
        for (int i = 0; i < list.length; i++) {
            rb[i] = new RadioButton(this);
            if (i == defaultCheckedId) rb[i].setChecked(true);
            rb[i].setPadding(15, 15, 15, 15);
            rb[i].setText(list[i]);
            rb[i].setTextSize(convertSizeToDp(mediumSize));
            rb[i].setId(i);
            rb[i].setGravity(Gravity.RIGHT);
            rb[i].setTextColor(Color.BLACK);

            rg.addView(rb[i]);
        }
        rg.setOnCheckedChangeListener(listener);
        RelativeLayout.LayoutParams toggleP = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rg.setLayoutParams(toggleP);

        if (data instanceof Integer)
            pageLayouts[(Integer) data].addView(rg);
        else if (data instanceof ViewGroup)
            ((ViewGroup) data).addView(rg);
    }

    void AddDropdown(Object data, String[] list, AdapterView.OnItemSelectedListener listener) {
        LinearLayout holderLayout = new LinearLayout(this);
        holderLayout.setOrientation(LinearLayout.VERTICAL);
        holderLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        holderLayout.setPadding(15, 15, 15, 15);
        holderLayout.setGravity(Gravity.CENTER);

        Spinner sp = new Spinner(this, Spinner.MODE_DROPDOWN);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.argb(255, 233, 233, 233));
        drawable.setStroke(1, Color.BLACK);
        sp.setPopupBackgroundDrawable(drawable);
        sp.setBackground(drawable);

        sp.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                ((TextView) v).setTextColor(Color.WHITE);
                ((TextView) v).setTypeface(null, Typeface.BOLD);
                ((TextView) v).setGravity(Gravity.CENTER);

                return v;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);

                ((TextView) v).setTextColor(Color.WHITE);
                ((TextView) v).setTypeface(null, Typeface.BOLD);
                ((TextView) v).setGravity(Gravity.CENTER);

                return v;
            }
        };
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(dataAdapter);
        sp.setOnItemSelectedListener(listener);
        sp.setPadding(0, 5, 0, 5);

        holderLayout.addView(sp);

        if (data instanceof Integer)
            pageLayouts[(Integer) data].addView(holderLayout);
        else if (data instanceof ViewGroup)
            ((ViewGroup) data).addView(holderLayout);
    }

    void AddButton(Object data, String text, int width, int height, int padding, View.OnClickListener listener) {
        LinearLayout holderLayout = new LinearLayout(this);
        holderLayout.setOrientation(LinearLayout.VERTICAL);
        holderLayout.setLayoutParams(new LinearLayout.LayoutParams(width, height));
        holderLayout.setPadding(padding, padding, padding, padding);
        holderLayout.setGravity(Gravity.CENTER);

        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextColor(Color.BLACK);
        btn.setOnClickListener(listener);
        btn.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.argb(255, 244, 244, 244));
        drawable.setStroke(2, Color.argb(255, 0, 0, 0));

        btn.setBackground(drawable);

        holderLayout.addView(btn);

        if (data instanceof Integer)
            pageLayouts[(Integer) data].addView(holderLayout);
        else if (data instanceof ViewGroup)
            ((ViewGroup) data).addView(holderLayout);
    }

    float convertSizeToDp(float size) {
        return size * density;
    }

    int convertSizeToDp(int size) {
        return (int) (size * density);
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                try {
                    Point screenSize = new Point();
                    Display display = windowManager.getDefaultDisplay();
                    display.getRealSize(screenSize);

                    screenWidth = screenSize.x;
                    screenHeight = screenSize.y;

                    mainLayoutParams.width = layoutWidth;
                    mainLayoutParams.height = layoutHeight;
                    windowManager.updateViewLayout(mainLayout, mainLayoutParams);

                    canvasLayoutParams.width = screenWidth;
                    canvasLayoutParams.height = screenHeight;
                    windowManager.updateViewLayout(canvasLayout, canvasLayoutParams);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    };

    Thread mUpdateCanvas = new Thread() {
        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_DISPLAY);
            while (isAlive() && !isInterrupted()) {
                try {
                    long t1 = System.currentTimeMillis();
                    canvasLayout.postInvalidate();
                    long td = System.currentTimeMillis() - t1;
                    Thread.sleep(Math.max(Math.min(0, sleepTime - td), sleepTime));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    };

    Thread mUpdateThread = new Thread() {
        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_DISPLAY);
            while (isAlive() && !isInterrupted()) {
                try {
                    long t1 = System.currentTimeMillis();
                    Point screenSize = new Point();
                    Display display = windowManager.getDefaultDisplay();
                    display.getRealSize(screenSize);

                    if (screenWidth != screenSize.x || screenHeight != screenSize.y) {
                        handler.sendEmptyMessage(0);
                    }

                    long td = System.currentTimeMillis() - t1;
                    Thread.sleep(Math.max(Math.min(0, sleepTime - td), sleepTime));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    };
}
