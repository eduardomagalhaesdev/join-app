import { ICategoria } from 'app/entities/categoria/categoria.model';

export interface IProduto {
  id: number;
  nome?: string | null;
  quantidade?: number | null;
  categoria?: Pick<ICategoria, 'id' | 'nome'> | null;
}

export type NewProduto = Omit<IProduto, 'id'> & { id: null };
