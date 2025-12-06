package com.github.xpenatan.jParser.example.app.tests;

import com.github.xpenatan.jParser.example.app.CodeTest;
import com.github.xpenatan.jParser.example.testlib.CallbackClass;
import com.github.xpenatan.jParser.example.testlib.CallbackClassManual;
import com.github.xpenatan.jParser.example.testlib.DefaultCallbackClass;
import com.github.xpenatan.jParser.example.testlib.TestCallbackClass;
import com.github.xpenatan.jParser.example.testlib.TestObjectClass;

public class CallbackTest implements CodeTest {

    private static boolean testCallbackClass() {
        {
            TestCallbackClass test = new TestCallbackClass();
            try {
                boolean[] internal_onVoidCallback = { false };
                CallbackClass callback = new CallbackClass() {
                    @Override
                    public void onVoidCallback(TestObjectClass refData, TestObjectClass pointerData) {
                        System.out.println("onVoidCallback");
                        internal_onVoidCallback[0] = true;
                    }
                };
                test.callVoidCallback(callback);
                if(!(internal_onVoidCallback[0] == true)) {
                    throw new RuntimeException("internal_onVoidCallback[0] == true");
                }
            } catch(Throwable e) {
                e.printStackTrace();
                test.dispose();
                return false;
            }
            test.dispose();
        }
        {
            TestCallbackClass test = new TestCallbackClass();
            try {
                boolean[] internal_onIntCallback = { false };
                CallbackClass callback = new CallbackClass() {
                    @Override
                    public int onIntCallback(int intValue01, int intValue02) {
                        internal_onIntCallback[0] = true;
                        return 0;
                    }
                };
                test.callIntCallback(callback);
                if(!(internal_onIntCallback[0] == true)) {
                    throw new RuntimeException("internal_onIntCallback[0] == true");
                }
            } catch(Throwable e) {
                e.printStackTrace();
                test.dispose();
                return false;
            }
            test.dispose();
        }
        {
            TestCallbackClass test = new TestCallbackClass();
            try {
                test.set_intValue01(10);
                test.set_intValue02(3);
                CallbackClass callback = new DefaultCallbackClass();
                int value = test.callIntCallback(callback);
                if(!(value == 7)) {
                    throw new RuntimeException("value == 7: " + value);
                }
                callback.dispose();
            } catch(Throwable e) {
                e.printStackTrace();
                test.dispose();
                return false;
            }
            test.dispose();
        }
        {
            TestCallbackClass test = new TestCallbackClass();
            try {
                boolean[] internal_onFloatCallback = { false };
                CallbackClass callback = new CallbackClass() {
                    @Override
                    public float onFloatCallback(float floatValue01, float floatValue02) {
                        internal_onFloatCallback[0] = true;
                        return 0;
                    }
                };
                test.callFloatCallback(callback);
                if(!(internal_onFloatCallback[0] == true)) {
                    throw new RuntimeException("internal_onFloatCallback[0] == true");
                }
            } catch(Throwable e) {
                e.printStackTrace();
                test.dispose();
                return false;
            }
            test.dispose();
        }
        {
            TestCallbackClass test = new TestCallbackClass();
            try {
                boolean[] internal_onBoolCallback = { false };
                CallbackClass callback = new CallbackClass() {
                    @Override
                    public boolean onBoolCallback(boolean boolValue01) {
                        internal_onBoolCallback[0] = true;
                        return false;
                    }
                };
                test.callBoolCallback(callback);
                if(!(internal_onBoolCallback[0] == true)) {
                    throw new RuntimeException("internal_onBoolCallback[0] == true");
                }
            } catch(Throwable e) {
                e.printStackTrace();
                test.dispose();
                return false;
            }
            test.dispose();
        }
        {
            TestCallbackClass test = new TestCallbackClass();
            try {
                String text = "HELLO_WORLD";
                test.get_strValue01().append(text);
                final String[] internal_onStringCallback = new String[1];
                CallbackClass callback = new CallbackClass() {
                    @Override
                    public void onStringCallback(String strValue01) {
                        internal_onStringCallback[0] = strValue01;
                    }
                };
                test.callStringCallback(callback);
                if(!(text.equals(internal_onStringCallback[0]) == true)) {
                    throw new RuntimeException("text.equals(internal_onStringCallback[0]) == true");
                }
            } catch(Throwable e) {
                e.printStackTrace();
                test.dispose();
                return false;
            }
            test.dispose();
        }
        {
            TestCallbackClass test = new TestCallbackClass();
            try {
                int[] onUnsignedIntCallback = { 0 };
                CallbackClass callback = new CallbackClass() {
                    @Override
                    public int onUnsignedIntCallback(int unsignedInt) {
                        onUnsignedIntCallback[0] = unsignedInt;
                        return 2;
                    }
                };
                int i = test.callUnsignedIntCallback(callback);
                if(!(onUnsignedIntCallback[0] == 13 && i == 2)) {
                    throw new RuntimeException("onUnsignedIntCallback[0] == 13 && i == 2");
                }
            } catch(Throwable e) {
                e.printStackTrace();
                test.dispose();
                return false;
            }
            test.dispose();
        }
        {
            TestCallbackClass test = new TestCallbackClass();
            try {
                short[] onUnsignedShortCallback = { 0 };
                CallbackClass callback = new CallbackClass() {
                    @Override
                    public short onUnsignedShortCallback(short unsignedShort) {
                        onUnsignedShortCallback[0] = unsignedShort;
                        return 3;
                    }
                };
                short i = test.callUnsignedShortCallback(callback);
                if(!(onUnsignedShortCallback[0] == 12 && i == 3)) {
                    throw new RuntimeException("onUnsignedShortCallback[0] == 12 && i == 3");
                }
            } catch(Throwable e) {
                e.printStackTrace();
                test.dispose();
                return false;
            }
            test.dispose();
        }
        return true;
    }

    private static boolean testCallbackClassManual() {
        {
            TestCallbackClass test = new TestCallbackClass();
            try {
                boolean[] internal_onVoidCallback = { false };
                CallbackClassManual callback = new CallbackClassManual() {
                    @Override
                    public void internal_onVoidCallback(long refData, long pointerData) {
                        internal_onVoidCallback[0] = true;
                    }
                };
                test.callManualVoidCallback(callback);
                if(!(internal_onVoidCallback[0] == true)) {
                    throw new RuntimeException("internal_onVoidCallback[0] == true");
                }
            } catch(Throwable e) {
                e.printStackTrace();
                test.dispose();
                return false;
            }
            test.dispose();
        }
        {
            TestCallbackClass test = new TestCallbackClass();
            try {
                boolean[] internal_onIntCallback = { false };
                CallbackClassManual callback = new CallbackClassManual() {
                    @Override
                    public int internal_onIntCallback(int intValue01, int intValue02) {
                        internal_onIntCallback[0] = true;
                        return 0;
                    }
                };
                test.callManualIntCallback(callback);
                if(!(internal_onIntCallback[0] == true)) {
                    throw new RuntimeException("internal_onIntCallback[0] == true");
                }
            } catch(Throwable e) {
                e.printStackTrace();
                test.dispose();
                return false;
            }
            test.dispose();
        }
        {
            TestCallbackClass test = new TestCallbackClass();
            try {
                boolean[] internal_onFloatCallback = { false };
                CallbackClassManual callback = new CallbackClassManual() {
                    @Override
                    public float internal_onFloatCallback(float floatValue01, float floatValue02) {
                        internal_onFloatCallback[0] = true;
                        return 0;
                    }
                };
                test.callManualFloatCallback(callback);
                if(!(internal_onFloatCallback[0] == true)) {
                    throw new RuntimeException("internal_onFloatCallback[0] == true");
                }
            } catch(Throwable e) {
                e.printStackTrace();
                test.dispose();
                return false;
            }
            test.dispose();
        }
        {
            TestCallbackClass test = new TestCallbackClass();
            try {
                boolean[] internal_onBoolCallback = { false };
                CallbackClassManual callback = new CallbackClassManual() {
                    @Override
                    public boolean internal_onBoolCallback(boolean boolValue01) {
                        internal_onBoolCallback[0] = true;
                        return false;
                    }
                };
                test.callManualBoolCallback(callback);
                if(!(internal_onBoolCallback[0] == true)) {
                    throw new RuntimeException("internal_onBoolCallback[0] == true");
                }
            } catch(Throwable e) {
                e.printStackTrace();
                test.dispose();
                return false;
            }
            test.dispose();
        }
        {
            TestCallbackClass test = new TestCallbackClass();
            try {
                String text = "HELLO_WORLD";
                test.get_strValue01().append(text);
                final String[] internal_onStringCallback = new String[1];
                CallbackClassManual callback = new CallbackClassManual() {
                    @Override
                    public void internal_onStringCallback(String strValue01) {
                        internal_onStringCallback[0] = strValue01;
                    }
                };
                test.callManualStringCallback(callback);
                if(!(text.equals(internal_onStringCallback[0]) == true)) {
                    throw new RuntimeException("text.equals(internal_onStringCallback[0]) == true");
                }
            } catch(Throwable e) {
                e.printStackTrace();
                test.dispose();
                return false;
            }
            test.dispose();
        }
        return true;
    }

    @Override
    public boolean test() {
        return testCallbackClass() && testCallbackClassManual();
    }
}
