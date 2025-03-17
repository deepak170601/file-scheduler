package com.example.JsonGenerator.service;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class GenerateJSONFiles {
    public static void main(String[] args) {
        String folderPath = "JSON";
        int totalJobIds = 6;      // 6 job ids
        int raysPerJob = 7;       // 7 ray ids per job (42 files total)

        // Create folder if it doesn't exist
        java.io.File folder = new java.io.File(folderPath);
        if (!folder.exists()) {
            folder.mkdir();
        }

        int fileCounter = 0; // overall file counter (from 1 to 42)

        for (int jobId = 1; jobId <= totalJobIds; jobId++) {
            // Each jobInfo's referenceIdentifier is based on jobId
            String jobIdentifier = "Dev_JobId_" + String.format("%03d", jobId);

            // For each job, generate 7 ray id files
            for (int rayId = 1; rayId <= raysPerJob; rayId++) {
                fileCounter++;
                // Overall unique ray identifier (e.g., Dev_RayId_001 to Dev_RayId_042)
                int overallRayId = (jobId - 1) * raysPerJob + rayId;
                String rayIdentifier = "Dev_RayId_" + String.format("%03d", overallRayId);
                String fileName = folderPath + "/" + rayIdentifier + ".json";

                // Build notificationInfo object
                JSONObject notificationInfo = new JSONObject();
                notificationInfo.put("stage", "Final");
                notificationInfo.put("source", "iPlus");

                // Build trackingInfo object
                JSONObject trackingInfo = new JSONObject();
                trackingInfo.put("rayIdentifier", rayIdentifier);
                trackingInfo.put("sourceTypeIdentifier", "PayementTeam001");
                trackingInfo.put("state", "Success");

                // Build jobInfo object
                JSONObject jobInfo = new JSONObject();
                jobInfo.put("identifier", "Dev_ExtractId_001");
                jobInfo.put("referenceIdentifier", jobIdentifier);
                jobInfo.put("definition", "Payment_Extract_001");
                jobInfo.put("transactionCount", raysPerJob); // 7 transactions per job
                jobInfo.put("state", "Started");

                // Build genericExtract object
                JSONObject genericExtract = new JSONObject();
                genericExtract.put("pageNo", 1);
                // For pageDataId, use overallRayId (e.g., "540001", "540002", ...)
                genericExtract.put("pageDataId", "54000" + overallRayId);
                genericExtract.put("lineCount", 100);
                genericExtract.put("totalPage", 1);
                genericExtract.put("extractFileName", "GenericExtract\\summary");

                // Build traceables object with proper JSON arrays
                JSONObject traceables = new JSONObject();

                JSONArray paymentIdentifierArr = new JSONArray();
                paymentIdentifierArr.add("Pay_PaymentIdentifier");
                traceables.put("paymentIdentifier", paymentIdentifierArr);

                JSONArray rayIdentifierArr = new JSONArray();
                rayIdentifierArr.add("Pay_RayIdentifier");
                traceables.put("rayIdentifier", rayIdentifierArr);

                JSONArray jobIdentifierArr = new JSONArray();
                jobIdentifierArr.add("Pay_JobIdentifier");
                traceables.put("jobIdentifier", jobIdentifierArr);

                // Generate 5 claim IDs sequentially.
                JSONArray claimIdsArray = new JSONArray();
                for (int i = 0; i < 5; i++) {
                    // (fileCounter-1)*5 ensures sequential claim numbering across files
                    int claimNumber = 11000 + ((fileCounter - 1) * 5) + i + 1;
                    String claimId = "C" + claimNumber;
                    claimIdsArray.add(claimId);
                }
                traceables.put("claimIds", claimIdsArray);
                genericExtract.put("traceables", traceables);

                // Build the transaction object
                JSONObject transactionObj = new JSONObject();
                // recordIdentifier is based on overallRayId (formatted to three digits)
                transactionObj.put("recordIdentifier", "Dev_PageId_" + String.format("%03d", overallRayId));
                transactionObj.put("genericExtract", genericExtract);

                // Build processStageInfo array with fixed process stages
                JSONArray processStageInfo = new JSONArray();

                JSONObject processStage1 = new JSONObject();
                processStage1.put("processStageName", "GENERIC-EXTRACTION-SERVICE - createFolder");
                processStage1.put("processStatus", "PROCESS_START");
                processStage1.put("auditTime", "2025-03-03 16:22:04.127");
                processStageInfo.add(processStage1);

                JSONObject processStage2 = new JSONObject();
                processStage2.put("processStageName", "GENERIC-EXTRACTION-SERVICE - createFolder");
                processStage2.put("processStatus", "PROCESS_END");
                processStage2.put("auditTime", "2025-03-03 16:24:04.127");
                processStageInfo.add(processStage2);

                JSONObject processStage3 = new JSONObject();
                processStage3.put("processStageName", "GENERIC-EXTRACTION-SERVICE – page data identifiers");
                processStage3.put("processStatus", "MESSAGE_PUBLISH");
                processStage3.put("auditTime", "2025-03-03 16:33:33.130");
                processStageInfo.add(processStage3);

                JSONObject processStage4 = new JSONObject();
                processStage4.put("processStageName", "GENERIC-EXTRACTION-SERVICE - FileGeneration");
                processStage4.put("processStatus", "SUCCESS");
                processStage4.put("auditTime", "2025-03-03 16:40:25.130");
                processStageInfo.add(processStage4);

                transactionObj.put("processStageInfo", processStageInfo);

                // Build transactionInfo array containing the single transaction object
                JSONArray transactionInfoArray = new JSONArray();
                transactionInfoArray.add(transactionObj);

                // Build the main JSON object
                JSONObject mainObject = new JSONObject();
                mainObject.put("notificationInfo", notificationInfo);
                mainObject.put("trackingInfo", trackingInfo);
                mainObject.put("jobInfo", jobInfo);
                mainObject.put("transactionInfo", transactionInfoArray);

                // Write the JSON object to file
                try (PrintWriter out = new PrintWriter(new FileWriter(fileName))) {
                    out.println(mainObject.toJSONString());
                    System.out.println("File saved to: " + fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Wait for 1 minute (60000 milliseconds) before generating the next file,
                // unless this is the last file
                if (!(jobId == totalJobIds && rayId == raysPerJob)) {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
