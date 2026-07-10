package com.unisound.vui.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class ShellUtils {
    public static final String COMMAND_EXIT = "exit\n";
    public static final String COMMAND_LINE_END = "\n";
    public static final String COMMAND_SH = "sh";
    public static final String COMMAND_SU = "su";

    public static class CommandResult {
        public String errorMsg;
        public int result;
        public String successMsg;

        public CommandResult(int result) {
            this.result = result;
        }

        public CommandResult(int result, String successMsg, String errorMsg) {
            this.result = result;
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }

        public String toString() {
            return "CommandResult{result=" + this.result + ", successMsg='" + this.successMsg + "', errorMsg='" + this.errorMsg + "'}";
        }
    }

    private ShellUtils() {
        throw new AssertionError();
    }

    public static boolean checkRootPermission() {
        return execCommand("echo root", true, false).result == 0;
    }

    public static CommandResult execCommand(String command, boolean isRoot) {
        return execCommand(new String[]{command}, isRoot, true);
    }

    public static CommandResult execCommand(String command, boolean isRoot, boolean isNeedResultMsg) {
        return execCommand(new String[]{command}, isRoot, isNeedResultMsg);
    }

    public static CommandResult execCommand(List<String> commands, boolean isRoot) {
        return execCommand(commands == null ? null : (String[]) commands.toArray(new String[0]), isRoot, true);
    }

    public static CommandResult execCommand(List<String> commands, boolean isRoot, boolean isNeedResultMsg) {
        return execCommand(commands == null ? null : (String[]) commands.toArray(new String[0]), isRoot, isNeedResultMsg);
    }

    public static CommandResult execCommand(String[] commands, boolean isRoot) {
        return execCommand(commands, isRoot, true);
    }

    /* JADX WARN: Removed duplicated region for block: B:159:? A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:32:0x005b  */
    /* JADX WARN: Removed duplicated region for block: B:35:0x0062  */
    /* JADX WARN: Removed duplicated region for block: B:37:0x0065  */
    /* JADX WARN: Removed duplicated region for block: B:64:0x00d4  */
    /* JADX WARN: Removed duplicated region for block: B:89:0x0115  */
    /* JADX WARN: Removed duplicated region for block: B:93:0x011e  */
    /* JADX WARN: Removed duplicated region for block: B:94:0x0124  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static CommandResult execCommand(String[] commands, boolean isRoot, boolean isNeedResultMsg) throws Throwable {
        DataOutputStream dataOutputStream;
        Process processExec;
        BufferedReader bufferedReader;
        Exception exc;
        DataOutputStream dataOutputStream2;
        StringBuilder sb;
        BufferedReader bufferedReader2;
        StringBuilder sb2;
        IOException iOException;
        StringBuilder sb3;
        BufferedReader bufferedReader3 = null;
        int iWaitFor = -1;
        if (commands != null) {
            try {
                if (commands.length != 0) {
                    try {
                        processExec = Runtime.getRuntime().exec(isRoot ? COMMAND_SU : "sh");
                        try {
                            dataOutputStream = new DataOutputStream(processExec.getOutputStream());
                            try {
                                try {
                                    for (String str : commands) {
                                        if (str != null) {
                                            dataOutputStream.write(str.getBytes());
                                            dataOutputStream.writeBytes("\n");
                                            dataOutputStream.flush();
                                        }
                                    }
                                    dataOutputStream.writeBytes(COMMAND_EXIT);
                                    dataOutputStream.flush();
                                    iWaitFor = processExec.waitFor();
                                } catch (Throwable th) {
                                    th = th;
                                    bufferedReader = null;
                                }
                            } catch (IOException e) {
                                dataOutputStream2 = dataOutputStream;
                                sb = null;
                                bufferedReader2 = null;
                                bufferedReader = null;
                                iOException = e;
                                sb2 = null;
                            } catch (Exception e2) {
                                dataOutputStream2 = dataOutputStream;
                                sb = null;
                                bufferedReader2 = null;
                                bufferedReader = null;
                                exc = e2;
                                sb2 = null;
                            }
                        } catch (IOException e3) {
                            iOException = e3;
                            dataOutputStream2 = null;
                            sb = null;
                            bufferedReader2 = null;
                            bufferedReader = null;
                            sb2 = null;
                        } catch (Exception e4) {
                            exc = e4;
                            dataOutputStream2 = null;
                            sb = null;
                            bufferedReader2 = null;
                            bufferedReader = null;
                            sb2 = null;
                        } catch (Throwable th2) {
                            th = th2;
                            dataOutputStream = null;
                            bufferedReader = null;
                        }
                    } catch (IOException e5) {
                        iOException = e5;
                        dataOutputStream2 = null;
                        sb = null;
                        bufferedReader2 = null;
                        bufferedReader = null;
                        processExec = null;
                        sb2 = null;
                    } catch (Exception e6) {
                        exc = e6;
                        dataOutputStream2 = null;
                        sb = null;
                        bufferedReader2 = null;
                        bufferedReader = null;
                        processExec = null;
                        sb2 = null;
                    } catch (Throwable th3) {
                        th = th3;
                        dataOutputStream = null;
                        bufferedReader = null;
                        processExec = null;
                    }
                    if (isNeedResultMsg) {
                        StringBuilder sb4 = new StringBuilder();
                        try {
                            sb3 = new StringBuilder();
                            try {
                                bufferedReader = new BufferedReader(new InputStreamReader(processExec.getInputStream()));
                                try {
                                    bufferedReader2 = new BufferedReader(new InputStreamReader(processExec.getErrorStream()));
                                } catch (IOException e7) {
                                    bufferedReader2 = null;
                                    iOException = e7;
                                    sb2 = sb3;
                                    sb = sb4;
                                    dataOutputStream2 = dataOutputStream;
                                } catch (Exception e8) {
                                    bufferedReader2 = null;
                                    exc = e8;
                                    sb2 = sb3;
                                    sb = sb4;
                                    dataOutputStream2 = dataOutputStream;
                                } catch (Throwable th4) {
                                    th = th4;
                                }
                            } catch (IOException e9) {
                                bufferedReader2 = null;
                                bufferedReader = null;
                                sb = sb4;
                                dataOutputStream2 = dataOutputStream;
                                iOException = e9;
                                sb2 = sb3;
                            } catch (Exception e10) {
                                bufferedReader2 = null;
                                bufferedReader = null;
                                sb = sb4;
                                dataOutputStream2 = dataOutputStream;
                                exc = e10;
                                sb2 = sb3;
                            }
                        } catch (IOException e11) {
                            sb = sb4;
                            bufferedReader2 = null;
                            bufferedReader = null;
                            dataOutputStream2 = dataOutputStream;
                            iOException = e11;
                            sb2 = null;
                        } catch (Exception e12) {
                            sb = sb4;
                            bufferedReader2 = null;
                            bufferedReader = null;
                            dataOutputStream2 = dataOutputStream;
                            exc = e12;
                            sb2 = null;
                        }
                        while (true) {
                            try {
                                String line = bufferedReader.readLine();
                                if (line == null) {
                                    break;
                                }
                                sb4.append(line);
                            } catch (IOException e13) {
                                sb2 = sb3;
                                sb = sb4;
                                dataOutputStream2 = dataOutputStream;
                                iOException = e13;
                                iOException.printStackTrace();
                                if (dataOutputStream2 != null) {
                                    try {
                                        dataOutputStream2.close();
                                    } catch (IOException e14) {
                                        e14.printStackTrace();
                                        if (processExec != null) {
                                            processExec.destroy();
                                        }
                                        return new CommandResult(iWaitFor, sb != null ? null : sb.toString(), sb2 != null ? null : sb2.toString());
                                    }
                                }
                                if (bufferedReader != null) {
                                    bufferedReader.close();
                                }
                                if (bufferedReader2 != null) {
                                    bufferedReader2.close();
                                }
                                if (processExec != null) {
                                }
                            } catch (Exception e15) {
                                sb2 = sb3;
                                sb = sb4;
                                dataOutputStream2 = dataOutputStream;
                                exc = e15;
                                exc.printStackTrace();
                                if (dataOutputStream2 != null) {
                                    try {
                                        dataOutputStream2.close();
                                    } catch (IOException e16) {
                                        e16.printStackTrace();
                                        if (processExec != null) {
                                            processExec.destroy();
                                        }
                                        return new CommandResult(iWaitFor, sb != null ? null : sb.toString(), sb2 != null ? null : sb2.toString());
                                    }
                                }
                                if (bufferedReader != null) {
                                    bufferedReader.close();
                                }
                                if (bufferedReader2 != null) {
                                    bufferedReader2.close();
                                }
                                if (processExec != null) {
                                }
                            } catch (Throwable th5) {
                                th = th5;
                                bufferedReader3 = bufferedReader2;
                                if (dataOutputStream != null) {
                                    try {
                                        dataOutputStream.close();
                                    } catch (IOException e17) {
                                        e17.printStackTrace();
                                        if (processExec != null) {
                                            throw th;
                                        }
                                        processExec.destroy();
                                        throw th;
                                    }
                                }
                                if (bufferedReader != null) {
                                    bufferedReader.close();
                                }
                                if (bufferedReader3 != null) {
                                    bufferedReader3.close();
                                }
                                if (processExec != null) {
                                }
                            }
                            return new CommandResult(iWaitFor, sb != null ? null : sb.toString(), sb2 != null ? null : sb2.toString());
                        }
                        while (true) {
                            String line2 = bufferedReader2.readLine();
                            if (line2 == null) {
                                break;
                            }
                            sb3.append(line2);
                        }
                        sb2 = sb3;
                        sb = sb4;
                    } else {
                        sb2 = null;
                        sb = null;
                        bufferedReader2 = null;
                        bufferedReader = null;
                    }
                    if (dataOutputStream != null) {
                        try {
                            dataOutputStream.close();
                        } catch (IOException e18) {
                            e18.printStackTrace();
                        }
                    }
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    if (bufferedReader2 != null) {
                        bufferedReader2.close();
                    }
                    if (processExec != null) {
                        processExec.destroy();
                    }
                    return new CommandResult(iWaitFor, sb != null ? null : sb.toString(), sb2 != null ? null : sb2.toString());
                }
            } catch (Throwable th6) {
                th = th6;
                dataOutputStream = dataOutputStream2;
                bufferedReader3 = bufferedReader2;
            }
        }
        return new CommandResult(-1, null, null);
    }
}
