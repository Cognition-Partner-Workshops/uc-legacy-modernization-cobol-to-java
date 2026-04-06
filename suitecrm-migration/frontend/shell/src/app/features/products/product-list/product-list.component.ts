import { Component, OnInit } from '@angular/core';
import { ProductCatalogService, Product, ProductCategory } from '../../../services/product-catalog.service';

@Component({
  selector: 'app-product-list',
  template: `
    <div class="product-list-container">
      <div class="toolbar">
        <h2>Product Catalog</h2>
        <div class="actions">
          <input type="text" placeholder="Search products..." (input)="onSearch($event)" />
          <button class="btn-primary">Add Product</button>
        </div>
      </div>
      <div class="product-layout">
        <aside class="category-filter">
          <h3>Categories</h3>
          <ul>
            <li (click)="clearCategory()" [class.active]="!selectedCategory">All Products</li>
            <li *ngFor="let cat of categories" (click)="filterByCategory(cat.id)" [class.active]="selectedCategory === cat.id">
              {{ cat.name }}
            </li>
          </ul>
          <h3>Quick Filters</h3>
          <button (click)="showLowStock()" class="btn-warning">Low Stock</button>
        </aside>
        <main>
          <table class="data-table">
            <thead>
              <tr><th>Name</th><th>Part #</th><th>List Price</th><th>Cost</th><th>Stock</th><th>Status</th></tr>
            </thead>
            <tbody>
              <tr *ngFor="let product of products" [routerLink]="[product.id]">
                <td>{{ product.name }}</td>
                <td>{{ product.partNumber }}</td>
                <td>{{ product.listPrice | currency }}</td>
                <td>{{ product.costPrice | currency }}</td>
                <td [class.low-stock]="product.qtyInStock < 10">{{ product.qtyInStock }}</td>
                <td><span class="badge" [ngClass]="product.status">{{ product.status }}</span></td>
              </tr>
            </tbody>
          </table>
        </main>
      </div>
    </div>
  `
})
export class ProductListComponent implements OnInit {
  products: Product[] = [];
  categories: ProductCategory[] = [];
  selectedCategory: string | null = null;

  constructor(private productService: ProductCatalogService) {}

  ngOnInit(): void {
    this.loadProducts();
    this.productService.getCategories().subscribe(cats => this.categories = cats);
  }

  loadProducts(): void {
    this.productService.getProducts().subscribe(data => this.products = data.content || []);
  }

  filterByCategory(categoryId: string): void {
    this.selectedCategory = categoryId;
    this.productService.getProductsByCategory(categoryId).subscribe(data => this.products = data.content || []);
  }

  clearCategory(): void {
    this.selectedCategory = null;
    this.loadProducts();
  }

  showLowStock(): void {
    this.productService.getLowStock().subscribe(data => this.products = data);
  }

  onSearch(event: Event): void {
    const query = (event.target as HTMLInputElement).value;
    if (query.length >= 3) {
      this.productService.searchProducts(query).subscribe(data => this.products = data.content || []);
    } else if (query.length === 0) {
      this.loadProducts();
    }
  }
}
