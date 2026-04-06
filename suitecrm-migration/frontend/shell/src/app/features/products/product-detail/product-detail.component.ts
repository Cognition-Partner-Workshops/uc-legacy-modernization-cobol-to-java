import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ProductCatalogService, Product } from '../../../services/product-catalog.service';

@Component({
  selector: 'app-product-detail',
  template: `
    <div class="product-detail" *ngIf="product">
      <h1>{{ product.name }}</h1>
      <div class="product-info-grid">
        <div class="info-card">
          <h3>Pricing</h3>
          <p><strong>List Price:</strong> {{ product.listPrice | currency }}</p>
          <p><strong>Cost Price:</strong> {{ product.costPrice | currency }}</p>
          <p><strong>Discount Price:</strong> {{ product.discountPrice | currency }}</p>
        </div>
        <div class="info-card">
          <h3>Inventory</h3>
          <p><strong>Part Number:</strong> {{ product.partNumber }}</p>
          <p><strong>Qty in Stock:</strong> {{ product.qtyInStock }}</p>
          <p><strong>Weight:</strong> {{ product.weight }}</p>
        </div>
        <div class="info-card">
          <h3>Details</h3>
          <p><strong>Status:</strong> <span class="badge" [ngClass]="product.status">{{ product.status }}</span></p>
          <p><strong>Manufacturer:</strong> {{ product.manufacturerName }}</p>
          <p><strong>Category:</strong> {{ product.categoryName }}</p>
        </div>
      </div>
      <div class="product-description">
        <h3>Description</h3>
        <p>{{ product.description }}</p>
      </div>
    </div>
  `
})
export class ProductDetailComponent implements OnInit {
  product: Product | null = null;

  constructor(private route: ActivatedRoute, private productService: ProductCatalogService) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.productService.getProduct(id).subscribe(p => this.product = p);
    }
  }
}
