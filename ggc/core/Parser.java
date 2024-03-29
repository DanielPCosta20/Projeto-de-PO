package ggc.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.Serializable;

import ggc.core.exception.BadEntryException;

public class Parser implements Serializable{

  private Warehouse _store;

  /**
   * 
   * @param w
   */
  public Parser(Warehouse w) {
    _store = w;
  }

  /**
   * 
   * @param filename
   * @throws IOException
   * @throws BadEntryException
   */
  void parseFile(String filename) throws IOException, BadEntryException {
    try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
      String line;

      while ((line = reader.readLine()) != null)
        parseLine(line);
    }
  }

  /**
   * 
   * @param line
   * @throws BadEntryException
   * @throws BadEntryException
   */
  private void parseLine(String line) throws BadEntryException, BadEntryException {
    String[] components = line.split("\\|");

    switch (components[0]) {
      case "PARTNER":
        parsePartner(components, line);
        break;
      case "BATCH_S":
        parseSimpleProduct(components, line);
        break;
      case "BATCH_M":
        parseAggregateProduct(components, line);
        break;
      default:
        throw new BadEntryException("Invalid type element: " + components[0]);
    }
  }

  /**
   * 
   * @param components
   * @param line
   * @throws BadEntryException
   */
  //PARTNER|id|nome|endereço
  private void parsePartner(String[] components, String line) throws BadEntryException {
    if (components.length != 4)
      throw new BadEntryException("Invalid partner with wrong number of fields (4): " + line);
    
    String id = components[1];
    String name = components[2];
    String address = components[3];
  
    _store.registerPartner(id, name, address);
  }

  /**
   * 
   * @param components
   * @param line
   * @throws BadEntryException
   */
  //BATCH_S|idProduto|idParceiro|prec ̧o|stock-actual
  private void parseSimpleProduct(String[] components, String line) throws BadEntryException {
    if (components.length != 5)
      throw new BadEntryException("Invalid number of fields (4) in simple batch description: " + line);
    
    String idProduct = components[1];
    String idPartner = components[2];
    double price = Double.parseDouble(components[3]);
    int stock = Integer.parseInt(components[4]);
    
    _store.registerSimpleProduct(idProduct, stock, true,price);
    
    Product product = _store.getProduct(idProduct);
    Partner partner = _store.getPartner(idPartner);

    Batch batch = _store.registerBatch(product, partner, price, stock);
    product.addBatch(batch);
    partner.addBatch(batch);
  }
 
  /**
   * 
   * @param components
   * @param line
   * @throws BadEntryException
   */
  //BATCH_M|idProduto|idParceiro|prec ̧o|stock-actual|agravamento|componente-1:quantidade-1#...#componente-n:quantidade-n
  private void parseAggregateProduct(String[] components, String line) throws BadEntryException {
    if (components.length != 7)
        throw new BadEntryException("Invalid number of fields (7) in aggregate batch description: " + line);
      
      String idProduct = components[1];
      String idPartner = components[2];
      double price = Double.parseDouble(components[3]);

      ArrayList<Product> products = new ArrayList<>();
      ArrayList<Integer> quantities = new ArrayList<>();
      
      for (String component : components[6].split("#")) {
        String[] recipeComponent = component.split(":"); 
        products.add(_store.getProduct(recipeComponent[0]));
        quantities.add(Integer.parseInt(recipeComponent[1]));
      }

      Set<Component> Componentes = new HashSet<>();

      for(int i=0; i < products.size();i++){
        Component comp = new Component(quantities.get(i),products.get(i));
        Componentes.add(comp);
      }

      Recipe _recipe = new Recipe(Double.parseDouble(components[5]), Componentes);
      int stock = Integer.parseInt(components[4]);
      _store.registerAggregateProduct(idProduct,stock, _recipe,price);
      
      Product product = _store.getProduct(idProduct);
      Partner partner = _store.getPartner(idPartner);
      Batch batch = _store.registerBatch(product, partner, price, stock);
      product.addBatch(batch);
      partner.addBatch(batch);
}}