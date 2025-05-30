import { Component, Input, OnInit } from '@angular/core';
import { ProductService } from '../../../services/products.service';

@Component({
    selector: 'app-compare-table',
    templateUrl: './compare-table.component.html',
    styleUrls: ['./compare-table.component.css'],
})
export class CompareTableComponent implements OnInit {
    @Input() productId!: string;
    isLoading: boolean = true;
    comparisons: any[] = [];
    errorMessage: string | null = null;
    minPrice: number = Infinity;

    constructor(private productService: ProductService) {}

    ngOnInit(): void {
        if (this.productId) {
            this.fetchComparisons(this.productId);
        }
    }

    fetchComparisons(productId: string): void {
        this.productService.compareProducts(productId).subscribe({
            next: (response) => {
                this.isLoading = false;
                if (response.data) {
                    this.comparisons = Object.values(response.data); 
                    this.comparisons.map((comparison) => {
                        if (comparison.price.total < this.minPrice) {
                            this.minPrice = comparison.price.total;
                        }
                    })
                } else {
                    this.errorMessage = 'No se encontraron productos para comparar.';
                }
            },
            error: (error) => {
                console.error('Error al obtener los datos de comparación:', error);
                this.errorMessage = 'Ocurrió un error al cargar los datos de comparación.';
            },
        });
    }
}