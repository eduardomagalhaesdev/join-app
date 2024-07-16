import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

@NgModule({
  imports: [
    RouterModule.forChild([
      {
        path: 'produto',
        data: { pageTitle: 'joinAppApp.produto.home.title' },
        loadChildren: () => import('./produto/produto.module').then(m => m.ProdutoModule),
      },
      {
        path: 'categoria',
        data: { pageTitle: 'joinAppApp.categoria.home.title' },
        loadChildren: () => import('./categoria/categoria.module').then(m => m.CategoriaModule),
      },
      /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
    ]),
  ],
})
export class EntityRoutingModule {}
