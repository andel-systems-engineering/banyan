package com.messagebus.benchmark.client;

import com.messagebus.client.Messagebus;
import com.messagebus.client.MessagebusSinglePool;
import com.messagebus.client.MessagebusUnOpenException;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageType;
import com.messagebus.common.ExceptionHelper;
import com.messagebus.common.TestVariableInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;

public class TestUtility {

    private final static Log logger = LogFactory.getLog(TestUtility.class);

    public static void writeFile(String fileName, long[] xArr, long[] yArr) {
        String filePath = String.format(TestConfigConstant.OUTPUT_FILE_PATH_FORMAT, fileName);

        File dataFile = new File(filePath);
        try {
            if (!dataFile.exists() && (!dataFile.createNewFile())) {
                throw new RuntimeException("create new file at : " + filePath + " , failure.");
            }
        } catch (IOException e) {
            ExceptionHelper.logException(logger, e, "writeFile");
            throw new RuntimeException(e);
        }

        FileWriter fileWriter = null;
        PrintWriter out = null;
        try {
            fileWriter = new FileWriter(filePath);
            out = new PrintWriter(fileWriter);
            out.println("#x y");

            for (int i = 0; i < xArr.length; i++) {
                out.println(xArr[i] + " " + yArr[i]);
            }

            out.flush();
        } catch (IOException e) {
            ExceptionHelper.logException(logger, e, "writeFile");
            throw new RuntimeException(e);
        } finally {
            try {
                if (fileWriter != null) fileWriter.close();
                if (out != null) out.close();
            } catch (IOException e) {

            }
        }
    }

    public static void exec(String[] cmds, boolean hasOutput) {
        if (cmds == null || cmds.length == 0) {
            return;
        }

        if (hasOutput) {
            Process process = null;
            try {
                process = Runtime.getRuntime().exec(cmds);
            } catch (IOException e) {
                ExceptionHelper.logException(logger, e, "exec");
                throw new RuntimeException(e.toString());
            }

            InputStreamReader ir = null;
            LineNumberReader input = null;
            try {
                ir = new InputStreamReader(process.getInputStream());
                input = new LineNumberReader(ir);
                String line;
                while ((line = input.readLine()) != null)
                    System.out.println(line);
            } catch (IOException e) {
                ExceptionHelper.logException(logger, e, "exec");
                throw new RuntimeException(e.toString());
            } finally {
                try {
                    if (ir != null) ir.close();
                    if (input != null) input.close();
                } catch (IOException e) {

                }
            }
        }
    }

    public static void produce(long total) {
        Message msg = TestMessageFactory.create(MessageType.QueueMessage, TestConfigConstant.MSG_BODY_SIZE_OF_BYTE);

        MessagebusSinglePool singlePool = new MessagebusSinglePool(TestVariableInfo.PUBSUBER_HOST,
                                                                   TestVariableInfo.PUBSUBER_PORT);
        Messagebus client = singlePool.getResource();

        try {
            for (int i = 0; i < total; i++) {
                client.produce(TestConfigConstant.PRODUCER_SECRET, TestConfigConstant.CONSUMER_QUEUE_NAME, msg, TestConfigConstant.PRODUCER_TOKEN);
            }
        } catch (MessagebusUnOpenException e) {
            e.printStackTrace();
        } finally {
            singlePool.returnResource(client);
            singlePool.destroy();
        }
    }

}
