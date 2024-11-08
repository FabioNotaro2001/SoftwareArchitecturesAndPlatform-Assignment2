package sap.ass2.ebikes.application;

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
import sap.ass2.ebikes.domain.Ebike;
import sap.ass2.ebikes.domain.Ebike.EbikeState;
import sap.ass2.ebikes.domain.EbikeRepository;
import sap.ass2.ebikes.domain.P2d;
import sap.ass2.ebikes.domain.RepositoryException;
import sap.ass2.ebikes.domain.V2d;

public class EbikeRepositoryImpl implements EbikeRepository {

    private String dbaseFolder;            // Base folder for storing database files.

    public EbikeRepositoryImpl() {
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
    public void saveEbike(Ebike eBike) throws RepositoryException {
        // Create a JsonObject representing the eBike's data.
		JsonObject obj = new JsonObject();
		obj.put("ID", eBike.getId());
		obj.put("STATE", eBike.getState().toString());
		obj.put("LOC_X", eBike.getLocation().x());
		obj.put("LOC_Y", eBike.getLocation().y());
		obj.put("DIR_X", eBike.getDirection().x());
		obj.put("DIR_Y", eBike.getDirection().y());
		obj.put("SPEED", eBike.getSpeed());
		obj.put("BATTERY", eBike.getBatteryLevel());

		// Save the JsonObject to a file under the eBike path.
		this.saveObj(eBike.getId(), obj);
    }

    @Override
    public List<Ebike> getEbikes() throws RepositoryException {
        List<Ebike> ebikes = new ArrayList<>();
		File ebikeDir = new File(dbaseFolder);  // eBike data directory.
		
		// Check if the directory exists and contains files.
		if (ebikeDir.exists() && ebikeDir.isDirectory()) {
			File[] ebikeFiles = ebikeDir.listFiles((dir, name) -> name.endsWith(".json"));  // Filter .json files.
			
			if (ebikeFiles != null) {
				for (File ebikeFile : ebikeFiles) {
					try {
						// Read file content and convert it to JsonObject.
						String content = new String(Files.readAllBytes(ebikeFile.toPath()));
						JsonObject obj = new JsonObject(content);
						
						// Create an EBike object from the JsonObject.
						Ebike ebike = new Ebike(
							obj.getString("ID"),
							EbikeState.valueOf(obj.getString("STATE")),
							new P2d(obj.getDouble("LOC_X"), obj.getDouble("LOC_Y")),
							new V2d(obj.getDouble("DIR_X"), obj.getDouble("DIR_Y")),
							obj.getDouble("SPEED"),
							obj.getInteger("BATTERY")
						);
						ebikes.add(ebike);
					} catch (IOException e) {
						throw new RepositoryException();  // Handle file read error.
					}
				}
			}
		}
		return ebikes;  // Return the list of eBikes.
    }

    @Override
    public Optional<Ebike> getEbikeByID(String id) throws RepositoryException {
        File ebikeFile = new File(Path.of(dbaseFolder, id + ".json").toString());

		// Check if the file exists.
		if (!ebikeFile.exists()) {
			return Optional.empty();
		} else {
			try {
				// Read file content and convert to JsonObject.
				String content = new String(Files.readAllBytes(ebikeFile.toPath()));
				JsonObject obj = new JsonObject(content);

				// Return an EBike object wrapped in an Optional.
				return Optional.of(new Ebike(
					obj.getString("ID"),
					EbikeState.valueOf(obj.getString("STATE")),
					new P2d(obj.getDouble("LOC_X"), obj.getDouble("LOC_Y")),
					new V2d(obj.getDouble("DIR_X"), obj.getDouble("DIR_Y")),
					obj.getDouble("SPEED"),
					obj.getInteger("BATTERY")
				));
			} catch (IOException e) {
				throw new RepositoryException();  // Handle file read error.
			}
		}
    }

}
