package sap.ass2.users.application;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.vertx.core.json.JsonObject;
import sap.ass2.users.domain.RepositoryException;
import sap.ass2.users.domain.User;
import sap.ass2.users.domain.UsersRepository;

public class UsersRepositoryImpl implements UsersRepository {

    private String dbaseFolder;            // Base folder for storing database files.

    public UsersRepositoryImpl() {
        this.dbaseFolder =  "./database";  // Default path for the database folder.
        makeDir(dbaseFolder);  // Create the base folder if not exists.
    }

    private void saveObj(String id, JsonObject obj) throws RepositoryException {
		try {
			// Open a file for writing (create a new one if it doesn't exist).
			FileWriter fw = new FileWriter(Path.of(dbaseFolder, id + ".json").toString());
			java.io.BufferedWriter wr = new BufferedWriter(fw);	
		
			// Write the JsonObject as a file.
			wr.write(obj.encodePrettily());
			wr.flush();
			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RepositoryException();  // Custom exception for repository errors.
		}
	}
	
	/**
	 * Creates a directory if it does not already exist.
	 * @param name Name (path) of the directory to create.
	 */
	private void makeDir(String name) {
		try {
			File dir = new File(name);
			if (!dir.exists()) {
				dir.mkdir();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


    @Override
    public void saveUser(User user) throws RepositoryException {
        // Create a JsonObject representing the user's data.
		JsonObject obj = new JsonObject();
		obj.put("ID", user.getId());
		obj.put("CREDIT", user.getCredit());

		// Save the JsonObject to a file under the user path.
		this.saveObj(user.getId(), obj);
    }

    @Override
    public List<User> getUsers() throws RepositoryException {
        List<User> users = new ArrayList<>();
		File userDir = new File(dbaseFolder);  // User data directory.
		
		// Check if the directory exists and contains files.
		if (userDir.exists() && userDir.isDirectory()) {
			File[] userFiles = userDir.listFiles((dir, name) -> name.endsWith(".json"));  // Filter .json files
			
			if (userFiles != null) {
				for (File userFile : userFiles) {
					try {
						// Read file content and convert it to JsonObject.
						String content = new String(Files.readAllBytes(userFile.toPath()));
						JsonObject obj = new JsonObject(content);
						
						// Create a User object from the JsonObject.
						User user = new User(obj.getString("ID"), obj.getInteger("CREDIT"));
						users.add(user);
					} catch (IOException e) {
						throw new RepositoryException();  // Handle file read error.
					}
				}
			}
		}
		return users;  // Return the list of users.
    }

    @Override
    public Optional<User> getUserByID(String id) throws RepositoryException {
        File userFile = new File(Path.of(dbaseFolder, id + ".json").toString());

		if (!userFile.exists()) {
			return Optional.empty();  
		} else {
			try {
				// Read file content and convert to JsonObject.
				String content = new String(Files.readAllBytes(userFile.toPath()));
				JsonObject obj = new JsonObject(content);
				
				// Return a User object wrapped in an Optional.
				return Optional.of(new User(obj.getString("ID"), obj.getInteger("CREDIT")));
			} catch (IOException e) {
				throw new RepositoryException();  // Handle file read error.
			}
		}
    }
}
