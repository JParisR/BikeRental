package es.udc.ws.bikes.restservice.json;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;

import es.udc.ws.bikes.dto.ServiceBikeDto;
import es.udc.ws.util.json.ObjectMapperFactory;
import es.udc.ws.util.json.exceptions.ParsingException;

public class JsonServiceBikeDtoConversor {
	
	public static ObjectNode toObjectNode(ServiceBikeDto bike) {
		ObjectNode bikeObject = JsonNodeFactory.instance.objectNode();
		
		if (bike.getBikeId() != null) {
			bikeObject.put("bikeId", bike.getBikeId());
		}
		bikeObject.put("description", bike.getDescription()).
			put("price", bike.getPrice()).
			put("units", bike.getUnits());
		
		return bikeObject;
	}
	
	public static ArrayNode toArrayNode(List<ServiceBikeDto> bikes) {

		ArrayNode bikesNode = JsonNodeFactory.instance.arrayNode();
		for (int i = 0; i < bikes.size(); i++) {
			ServiceBikeDto bikeDto = bikes.get(i);
			ObjectNode bikeObject = toObjectNode(bikeDto);
			bikesNode.add(bikeObject);
		}

		return bikesNode;
	}
	
	public static ServiceBikeDto toServiceBikeDto(InputStream jsonBike) throws ParsingException {
		try {
			ObjectMapper objectMapper = ObjectMapperFactory.instance();
			JsonNode rootNode = objectMapper.readTree(jsonBike);
			
			if (rootNode.getNodeType() != JsonNodeType.OBJECT) {
				throw new ParsingException("Unrecognized JSON (object expected)");
			} else {
				ObjectNode bikeObject = (ObjectNode) rootNode;

				JsonNode bikeIdNode = bikeObject.get("bikeId");
				Long bikeId = (bikeIdNode != null) ? bikeIdNode.longValue() : null;

				String description = bikeObject.get("description").textValue().trim();
				
				Long dateLong = bikeObject.get("startDate").asLong();
				Date date = new Date(dateLong);
				Calendar startDate = Calendar.getInstance();
				startDate.setTime(date);
				
				int units =  bikeObject.get("units").intValue();
				float price = bikeObject.get("price").floatValue();

				return new ServiceBikeDto(bikeId, description, price, units, startDate);
			}
		} catch (ParsingException ex) {
			throw ex;
		} catch (Exception e) {
			throw new ParsingException(e);
		}
	}
	
}
