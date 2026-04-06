import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Product {
  id: string;
  name: string;
  description: string;
  status: string;
  partNumber: string;
  costPrice: number;
  listPrice: number;
  discountPrice: number;
  categoryId: string;
  categoryName: string;
  manufacturerId: string;
  manufacturerName: string;
  qtyInStock: number;
  weight: number;
}

export interface ProductCategory {
  id: string;
  name: string;
  description: string;
  parentId: string;
  children: ProductCategory[];
}

@Injectable({ providedIn: 'root' })
export class ProductCatalogService {
  private apiUrl = `${environment.apiGatewayUrl}/product-catalog-service/api/v1/products`;

  constructor(private http: HttpClient) {}

  getProducts(page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get(this.apiUrl, { params });
  }

  getProductsByCategory(categoryId: string, page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get(`${this.apiUrl}/category/${categoryId}`, { params });
  }

  getProduct(id: string): Observable<Product> {
    return this.http.get<Product>(`${this.apiUrl}/${id}`);
  }

  createProduct(product: Partial<Product>): Observable<Product> {
    return this.http.post<Product>(this.apiUrl, product);
  }

  updateProduct(id: string, product: Partial<Product>): Observable<Product> {
    return this.http.put<Product>(`${this.apiUrl}/${id}`, product);
  }

  deleteProduct(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  searchProducts(query: string, page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('q', query).set('page', page).set('size', size);
    return this.http.get(`${this.apiUrl}/search`, { params });
  }

  getLowStock(threshold = 10): Observable<Product[]> {
    const params = new HttpParams().set('threshold', threshold);
    return this.http.get<Product[]>(`${this.apiUrl}/low-stock`, { params });
  }

  getCategories(): Observable<ProductCategory[]> {
    return this.http.get<ProductCategory[]>(`${this.apiUrl}/categories`);
  }

  createCategory(category: Partial<ProductCategory>): Observable<ProductCategory> {
    return this.http.post<ProductCategory>(`${this.apiUrl}/categories`, category);
  }

  getTypes(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/types`);
  }

  getManufacturers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/manufacturers`);
  }

  getShippers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/shippers`);
  }
}
