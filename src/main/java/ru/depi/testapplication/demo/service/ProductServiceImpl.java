package ru.depi.testapplication.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.depi.testapplication.demo.dto.ProductDTO;
import ru.depi.testapplication.demo.entity.Currency;
import ru.depi.testapplication.demo.entity.Info_language;
import ru.depi.testapplication.demo.entity.Product;
import ru.depi.testapplication.demo.repository.CurrencyRepository;
import ru.depi.testapplication.demo.repository.Info_languageRepository;
import ru.depi.testapplication.demo.repository.ProductRepository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author DePi
 **/

@Service
public class ProductServiceImpl implements ProductService {

    @Resource
    private ProductRepository productRepository;
    @Resource
    private CurrencyRepository currencyRepository;
    @Resource
    private Info_languageRepository info_languageRepository;

    @Override
    @Transactional
    public ProductDTO addProduct(ProductDTO productDTO) {
        Product product = new Product();
        mapDTOToEntity(productDTO, product);
        Product savedProduct = productRepository.save(product);
        return mapEntityToDTO(savedProduct);
    }

    @Override
    public List<ProductDTO> getAllProducts() {
        List<ProductDTO> productDTOS = new ArrayList<>();
        List<Product> products = productRepository.findAll();
        products.stream().forEach(product -> {
            ProductDTO productDTO = mapEntityToDTO(product);
            productDTOS.add(productDTO);
        });
        return productDTOS;
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(int id, ProductDTO productDTO) {
        Product product = productRepository.getOne(id);
        product.getCurrencies().clear();
        product.getLanguages().clear();
        mapDTOToEntity(productDTO, product);
        Product prd = productRepository.save(product);
        return mapEntityToDTO(prd);
    }

    @Override
    public String deleteProduct(int id) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            product.get().removeCurrencies();
            product.get().removeLanguages();
            productRepository.deleteById(product.get().getId());
            return "Product with id: " + id + " deleted successfully";
        }
        return null;
    }

    private void mapDTOToEntity(ProductDTO productDTO, Product product) {
        product.setName(productDTO.getName());
        if (null == product.getCurrencies()) {
            product.setCurrencies(new ArrayList<>());
        }
        productDTO.getCurrencies().stream().forEach(currencyName -> {
            Currency currency = currencyRepository.findByValue(currencyName);
            if (null == currency) {
                currency = new Currency();
                currency.setProducts(new ArrayList<>());
            }
            currency.setValue(currencyName);
            product.addCurrencyToProduct(currency);
        });

        if (null == product.getLanguages()) {
            product.setLanguages(new ArrayList<>());
        }
        productDTO.getLanguages().stream().forEach(languageName -> {
            Info_language language = info_languageRepository.findByLanguage(languageName);
            if (null == language) {
                language = new Info_language();
                language.setProducts(new ArrayList<>());
            }
            language.setLanguage(languageName);
            product.addLanguageToProduct(language);
        });
    }

    private ProductDTO mapEntityToDTO(Product product) {
        ProductDTO responseDto = new ProductDTO();
        responseDto.setName(product.getName());
        responseDto.setId(product.getId());
        responseDto.setCurrencies(product.getCurrencies().stream().map(Currency::getValue).collect(Collectors.toList()));
        responseDto.setLanguages(product.getLanguages().stream().map(Info_language::getLanguage).collect(Collectors.toList()));
        return responseDto;
    }
}
