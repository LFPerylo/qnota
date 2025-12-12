import * as React from "react";
import { Dialog, DialogContent } from "./dialog";
import { Button } from "./button";

interface AlertDialogProps {
  open?: boolean;
  onOpenChange?: (open: boolean) => void;
  children: React.ReactNode;
}

const AlertDialog = ({ open, onOpenChange, children }: AlertDialogProps) => {
  return <Dialog open={open} onOpenChange={onOpenChange}>{children}</Dialog>;
};

const AlertDialogContent = DialogContent;

const AlertDialogHeader = ({ className, ...props }: React.HTMLAttributes<HTMLDivElement>) => (
  <div className={className} {...props} />
);

const AlertDialogTitle = ({ className, ...props }: React.HTMLAttributes<HTMLHeadingElement>) => (
  <h2 className={className} {...props} />
);

const AlertDialogDescription = ({ className, ...props }: React.HTMLAttributes<HTMLParagraphElement>) => (
  <p className={className} {...props} />
);

const AlertDialogFooter = ({ className, ...props }: React.HTMLAttributes<HTMLDivElement>) => (
  <div className={className} {...props} />
);

const AlertDialogCancel = ({ onClick, ...props }: React.ButtonHTMLAttributes<HTMLButtonElement>) => (
  <Button variant="outline" onClick={onClick} {...props}>Cancelar</Button>
);

const AlertDialogAction = ({ onClick, ...props }: React.ButtonHTMLAttributes<HTMLButtonElement>) => (
  <Button onClick={onClick} {...props}>Confirmar</Button>
);

export {
  AlertDialog,
  AlertDialogContent,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogCancel,
  AlertDialogAction,
};


