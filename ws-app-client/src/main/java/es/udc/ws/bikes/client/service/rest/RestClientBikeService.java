package es.udc.ws.bikes.client.service.rest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.udc.ws.bikes.client.service.dto.ClientBikeDto;
import es.udc.ws.bikes.client.service.ClientBikeService;
import es.udc.ws.bikes.client.service.rest.json.JsonClientExceptionConversor;
import es.udc.ws.bikes.client.service.rest.json.JsonClientBikeDtoConversor;
import es.udc.ws.bikes.client.service.rest.json.JsonClientBookDtoConversor;
import es.udc.ws.util.configuration.ConfigurationParametersManager;
import es.udc.ws.util.exceptions.InputValidationException;
import es.udc.ws.util.exceptions.InstanceNotFoundException;
import es.udc.ws.bikes.client.service.exceptions.ClientInvalidStartDateException;
import es.udc.ws.util.json.ObjectMapperFactory;
import es.udc.ws.util.json.exceptions.ParsingException;

public class RestClientBikeService implements ClientBikeService{

    private final static String ENDPOINT_ADDRESS_PARAMETER = "RestClientBikeService.endpointAddress";
    private String endpointAddress;

    @Override
    public Long addBike(ClientBikeDto Bike) throws InputValidationException {

        try {

            HttpResponse response = Request.Post(getEndpointAddress() + "Bikes").
                    bodyStream(toInputStream(Bike), ContentType.create("application/json")).
                    execute().returnResponse();

            validateStatusCode(HttpStatus.SC_CREATED, response);

            return JsonClientBikeDtoConversor.toClientBikeDto(response.getEntity().getContent()).getBikeId();

        } catch (InputValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void updateBike(ClientBikeDto Bike) throws InputValidationException,
            InstanceNotFoundException {

        try {

            HttpResponse response = Request.Put(getEndpointAddress() + "Bikes/" + Bike.getBikeId()).
                    bodyStream(toInputStream(Bike), ContentType.create("application/json")).
                    execute().returnResponse();

            validateStatusCode(HttpStatus.SC_NO_CONTENT, response);

        } catch (InputValidationException | InstanceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public List<ClientBikeDto> findBikes(String keywords) {

        try {

            HttpResponse response = Request.Get(getEndpointAddress() + "Bikes?keywords="
                            + URLEncoder.encode(keywords, "UTF-8")).
                    execute().returnResponse();

            validateStatusCode(HttpStatus.SC_OK, response);

            return JsonClientBikeDtoConversor.toClientBikeDtos(response.getEntity()
                    .getContent());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Long bookBike(Long BikeId, String userId, String creditCardNumber)
            throws InstanceNotFoundException, InputValidationException {

        try {

            HttpResponse response = Request.Post(getEndpointAddress() + "Books").
                    bodyForm(
                            Form.form().
                            add("BikeId", Long.toString(BikeId)).
                            add("userId", userId).
                            add("creditCardNumber", creditCardNumber).
                            build()).
                    execute().returnResponse();

            validateStatusCode(HttpStatus.SC_CREATED, response);

            return JsonClientBookDtoConversor.toClientBookDto(
                    response.getEntity().getContent()).getBookId();

        } catch (InputValidationException | InstanceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
/*
    @Override
    public String getBikeUrl(Long BookId) throws InstanceNotFoundException,
            /*ClientBookExpirationException {
/*
        try {

            HttpResponse response = Request.Get(getEndpointAddress() + "Books/" + BookId).
                    execute().returnResponse();

            validateStatusCode(HttpStatus.SC_OK, response);

            return JsonClientBookDtoConversor.toClientBookDto(
                    response.getEntity().getContent()).getBikeUrl();

        } catch (InstanceNotFoundException | ClientBookExpirationException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
*/
    private synchronized String getEndpointAddress() {
        if (endpointAddress == null) {
            endpointAddress = ConfigurationParametersManager
                    .getParameter(ENDPOINT_ADDRESS_PARAMETER);
        }
        return endpointAddress;
    }

    private InputStream toInputStream(ClientBikeDto Bike) {

        try {

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectMapper objectMapper = ObjectMapperFactory.instance();
            objectMapper.writer(new DefaultPrettyPrinter()).writeValue(outputStream, JsonClientBikeDtoConversor.toJsonObject(Bike));

            return new ByteArrayInputStream(outputStream.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void validateStatusCode(int successCode, HttpResponse response)
            throws InstanceNotFoundException, ClientInvalidStartDateException,
            InputValidationException, ParsingException {

        try {

            int statusCode = response.getStatusLine().getStatusCode();

            /* Success? */
            if (statusCode == successCode) {
                return;
            }

            /* Handler error. */
            switch (statusCode) {

                case HttpStatus.SC_NOT_FOUND:
                    throw JsonClientExceptionConversor.fromInstanceNotFoundException(
                            response.getEntity().getContent());

                case HttpStatus.SC_BAD_REQUEST:
                    throw JsonClientExceptionConversor.fromInputValidationException(
                            response.getEntity().getContent());

                case HttpStatus.SC_GONE:
                    throw JsonClientExceptionConversor.fromInvalidStartDateException(
                            response.getEntity().getContent());

                default:
                    throw new RuntimeException("HTTP error; status code = "
                            + statusCode);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
