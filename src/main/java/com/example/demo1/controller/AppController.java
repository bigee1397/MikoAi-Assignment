package com.example.demo1.controller;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import redis.clients.jedis.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/database")
public class AppController {
//	private static final String METADATA_FILE = "./metadata.txt";
	@PostMapping("/executeStatements")
	public ResponseEntity<String> executeStatements(@RequestBody List<String> statements) {
        try {
            for (String statement : statements) {
                processStatement(statement);
            }
            return ResponseEntity.ok("Success: Statements executed successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failure: An error occurred while executing statements");
        }
    }
	
	private void processStatement(String statement) {
		if(statement.startsWith("CREATE TABLE")) {
			processCreateTable(statement);
		} else if (statement.startsWith("INSERT INTO")) {
			processInsertData(statement);
		}
	}
	
	private static void processCreateTable(String createTableQuery) {
		String tableName = extractTableName(createTableQuery);
        List<List<String>> columnInfoList = extractColumnInfo(createTableQuery);
//		List<String> columnInfoList = extractColumnInfo(createTableQuery);
		
		saveMetadata(tableName, columnInfoList);
	}

    private static void processInsertData(String insertDataQuery) {
        String tableName = extractTableNameFromInsert(insertDataQuery);
        List<List<String>> data = extractInsertData(insertDataQuery);
       
        insertData(tableName, data);
    }

    private static String extractTableName(String query) {
        Pattern pattern = Pattern.compile("CREATE TABLE (\\w+)");
        Matcher matcher = pattern.matcher(query);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("Invalid CREATE TABLE query");
    }

    private static String extractTableNameFromInsert(String query) {
        Pattern pattern = Pattern.compile("INSERT INTO (\\w+).*");
        Matcher matcher = pattern.matcher(query);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("Invalid INSERT INTO query");
    }

    private static List<List<String>> extractColumnInfo(String query) {
        Pattern pattern = Pattern.compile("\\((.*?)\\)");
        Matcher matcher = pattern.matcher(query);

        if (matcher.find()) {
        	String[] columns = matcher.group(1).split(",");
            List<List<String>> columnInfoList = new ArrayList<>();

            for (String column : columns) {
                String[] parts = column.trim().split("\\s+");
                String name = parts[0];
                String type = parts[1];

                // Create a pair [name, type] and add it to the list
                List<String> columnPair = new ArrayList<>();
                columnPair.add(name);
                columnPair.add(type);
                columnInfoList.add(columnPair);
            }
            return columnInfoList;
        }

        throw new IllegalArgumentException("Invalid CREATE TABLE query");
    }

    private static List<List<String>> extractInsertData(String query) {
        Pattern pattern = Pattern.compile("\\(([^)]+)\\)");

        Matcher matcher = pattern.matcher(query);
        List<List<String>> data = new ArrayList<>();

        while (matcher.find()) {
            String valuesInsideParentheses = matcher.group(1);
            String[] values = valuesInsideParentheses.split(",\\s*");

            List<String> rowData = new ArrayList<>();
            for (String value : values) {
                value = value.trim().replaceAll("'", "");
                rowData.add(value);
            }

            data.add(rowData);
        }
        return data;
    }
    

    private static void saveMetadata(String tableName, List<List<String>> columnInfoList) {
    	
        try (JedisPool poolConfig = new JedisPool(new JedisPoolConfig(),  "127.0.0.1", 6379);) {
        	try(Jedis jedis = poolConfig.getResource()) {
                Map<String, String> hash = new HashMap<>();
            	for (List<String> columnInfo : columnInfoList) {
                    hash.put(columnInfo.get(0),columnInfo.get(1));
                }
            	jedis.hmset(tableName + "-metadata", hash);
            } catch(Exception e) {
                e.printStackTrace();
            }
        } catch(Exception e) {
        	e.printStackTrace();
        }
//        poolConfig.setMaxTotal(10);
//        poolConfig.setMaxIdle(5);
//        poolConfig.setMinIdle(1);
//        poolConfig.setMaxWaitMillis(3000);
//        poolConfig.setTestOnBorrow(true);
//        System.out.println("Connection Successfullll 123456789");
        
//          System.out.println(columnInfoList.toString());
//         try (PrintWriter writer = new PrintWriter(new FileWriter(METADATA_FILE, true))) {
//             writer.println(tableName);
//             for (String columnInfo : columnInfoList) {
//                 writer.println(columnInfo);
//             }
//             writer.println(); // Separate tables with an empty line
//         } catch (IOException e) {
//                e.printStackTrace();
//         }
    }
    private static void insertData(String tableName, List<List<String>> data) {
       
    	try(JedisPool poolConfig = new JedisPool(new JedisPoolConfig(), "127.0.0.1", 6379);) {
    		try(Jedis jedis = poolConfig.getResource()) {
            	
    	    	for (List<String> innerList : data) {
    	            jedis.rpush(tableName + "-values", innerList.toArray(new String[0]));
    	        }
    	        	
    	        } catch(Exception e) {
    	        	e.printStackTrace();
    	        }
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
//    	poolConfig.setMaxTotal(10);
//    	poolConfig.setMaxIdle(5);
//    	poolConfig.setMinIdle(1);
//    	poolConfig.setMaxWaitMillis(3000);
//    	poolConfig.setTestOnBorrow(true);
        
    	
//    	try (PrintWriter writer = new PrintWriter(new FileWriter("./"+ tableName + ".txt", true))) {
//            writer.println(tableName + " Values");
//            for (List<String> row : data) {
////            	System.out.println(row.get(0));
//                writer.print(row.toString().substring(1, row.toString().length() - 1));
//                writer.println();
//            }
//            writer.println();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}

